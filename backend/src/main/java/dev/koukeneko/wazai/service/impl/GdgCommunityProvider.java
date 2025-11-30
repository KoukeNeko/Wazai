package dev.koukeneko.wazai.service.impl;

import dev.koukeneko.wazai.dto.Coordinates;
import dev.koukeneko.wazai.dto.WazaiEvent;
import dev.koukeneko.wazai.dto.WazaiMapItem;
import dev.koukeneko.wazai.dto.external.gdg.*;
import dev.koukeneko.wazai.service.ActivityProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static dev.koukeneko.wazai.dto.WazaiEvent.EventType;
import static dev.koukeneko.wazai.dto.WazaiMapItem.Country;
import static dev.koukeneko.wazai.dto.WazaiMapItem.DataSource;

/**
 * Provider for GDG (Google Developer Groups) Community events.
 *
 * Integrates with the official GDG Community APIs:
 * - /api/chapter_region?chapters=true - Fetches all GDG chapters with coordinates
 * - /api/search/ - Fetches upcoming events
 *
 * This provider filters for Taiwan (TW) chapters and uses official coordinates
 * from the GDG API instead of manual mapping.
 */
@Service
public class GdgCommunityProvider implements ActivityProvider {

    private static final String PROVIDER_NAME = "GDG Community";
    private static final String API_BASE_URL = "https://gdg.community.dev/api";
    private static final String CHAPTER_REGION_ENDPOINT = "/chapter_region";
    private static final String SEARCH_ENDPOINT = "/search/";
    private static final List<String> TARGET_COUNTRY_CODES = List.of("TW", "JP");
    private static final int DEFAULT_PROXIMITY_KM = 10000;

    private final WebClient webClient;
    private final Map<Long, GdgChapterInfo> chaptersCache = new HashMap<>();

    public GdgCommunityProvider(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(API_BASE_URL)
                .build();
        loadTaiwanChapters();
    }

    @Override
    public List<WazaiMapItem> search(String keyword) {
        if (isEmptyKeyword(keyword)) {
            return Collections.emptyList();
        }

        return fetchGdgEvents();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    private boolean isEmptyKeyword(String keyword) {
        return keyword == null || keyword.isBlank();
    }

    /**
     * Loads all target country GDG chapters from chapter_region API.
     * Filters by country codes (TW, JP) and caches the results.
     */
    private void loadTaiwanChapters() {
        try {
            System.out.println("[GDG] Loading GDG chapters from API...");
            List<GdgRegion> regions = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(CHAPTER_REGION_ENDPOINT)
                            .queryParam("chapters", "true")
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GdgRegion>>() {})
                    .block();

            if (regions == null || regions.isEmpty()) {
                System.out.println("[GDG] No regions found");
                return;
            }

            filterAndCacheTaiwanChapters(regions);

            long taiwanCount = chaptersCache.values().stream()
                    .filter(c -> "TW".equalsIgnoreCase(c.country()))
                    .count();
            long japanCount = chaptersCache.values().stream()
                    .filter(c -> "JP".equalsIgnoreCase(c.country()))
                    .count();

            System.out.println("[GDG] Loaded " + chaptersCache.size() + " chapters " +
                    "(TW: " + taiwanCount + ", JP: " + japanCount + ")");

        } catch (Exception e) {
            System.err.println("[GDG] Failed to load chapters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterAndCacheTaiwanChapters(List<GdgRegion> regions) {
        for (GdgRegion region : regions) {
            if (region.chapters() == null) {
                continue;
            }

            for (GdgChapterInfo chapter : region.chapters()) {
                if (isTargetChapter(chapter)) {
                    chaptersCache.put(chapter.id(), chapter);
                }
            }
        }
    }

    private boolean isTargetChapter(GdgChapterInfo chapter) {
        return chapter.country() != null
                && TARGET_COUNTRY_CODES.stream()
                        .anyMatch(code -> code.equalsIgnoreCase(chapter.country()))
                && chapter.active() != null
                && chapter.active();
    }

    private List<WazaiMapItem> fetchGdgEvents() {
        try {
            GdgApiResponse response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(SEARCH_ENDPOINT)
                            .queryParam("result_types", "upcoming_event")
                            .queryParam("order_by_proximity", "true")
                            .queryParam("proximity", DEFAULT_PROXIMITY_KM)
                            .build())
                    .retrieve()
                    .bodyToMono(GdgApiResponse.class)
                    .block();

            if (response == null || response.results() == null || response.results().isEmpty()) {
                return Collections.emptyList();
            }

            return transformGdgEvents(response.results());

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<WazaiMapItem> transformGdgEvents(List<GdgEvent> gdgEvents) {
        return gdgEvents.stream()
                .filter(this::isTaiwanEvent)
                .map(this::transformToWazaiEvent)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean isTaiwanEvent(GdgEvent event) {
        if (event.chapter() == null || event.chapter().id() == null) {
            return false;
        }
        return chaptersCache.containsKey(event.chapter().id());
    }

    private WazaiEvent transformToWazaiEvent(GdgEvent gdgEvent) {
        try {
            return new WazaiEvent(
                    generateEventId(gdgEvent.id()),
                    gdgEvent.title(),
                    extractDescription(gdgEvent),
                    gdgEvent.url(),
                    extractCoordinates(gdgEvent),
                    parseStartTime(gdgEvent.startDate()),
                    null,
                    EventType.COMMUNITY_GATHERING,
                    DataSource.GOOGLE_COMMUNITY,
                    determineCountry(gdgEvent)
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String generateEventId(Long gdgEventId) {
        return "gdg-" + gdgEventId;
    }

    private String extractDescription(GdgEvent event) {
        String desc = event.descriptionShort();
        if (desc == null || desc.isBlank()) {
            return "GDG Community Event";
        }
        return desc.length() > 500 ? desc.substring(0, 500) + "..." : desc;
    }

    /**
     * Extracts coordinates from cached chapter info.
     * Uses official coordinates from GDG API instead of manual mapping.
     */
    private Coordinates extractCoordinates(GdgEvent event) {
        if (event.chapter() == null || event.chapter().id() == null) {
            return Coordinates.taipei();
        }

        GdgChapterInfo chapterInfo = chaptersCache.get(event.chapter().id());
        if (chapterInfo == null
                || chapterInfo.latitude() == null
                || chapterInfo.longitude() == null) {
            return Coordinates.taipei();
        }

        return new Coordinates(chapterInfo.latitude(), chapterInfo.longitude());
    }

    private Country determineCountry(GdgEvent event) {
        if (event.chapter() == null || event.chapter().id() == null) {
            return Country.DEFAULT;
        }

        GdgChapterInfo chapterInfo = chaptersCache.get(event.chapter().id());
        if (chapterInfo == null || chapterInfo.country() == null) {
            return Country.DEFAULT;
        }

        return switch (chapterInfo.country().toUpperCase()) {
            case "TW" -> Country.TAIWAN;
            case "JP" -> Country.JAPAN;
            default -> Country.DEFAULT;
        };
    }

    private java.time.LocalDateTime parseStartTime(String startDateStr) {
        if (startDateStr == null || startDateStr.isBlank()) {
            return null;
        }

        try {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(startDateStr);
            return offsetDateTime.toLocalDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
