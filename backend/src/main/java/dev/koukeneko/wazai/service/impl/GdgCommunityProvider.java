package dev.koukeneko.wazai.service.impl;

import dev.koukeneko.wazai.dto.Coordinates;
import dev.koukeneko.wazai.dto.WazaiEvent;
import dev.koukeneko.wazai.dto.WazaiMapItem;
import dev.koukeneko.wazai.dto.external.gdg.GdgApiResponse;
import dev.koukeneko.wazai.dto.external.gdg.GdgChapter;
import dev.koukeneko.wazai.dto.external.gdg.GdgEvent;
import dev.koukeneko.wazai.service.ActivityProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.koukeneko.wazai.dto.WazaiEvent.EventType;
import static dev.koukeneko.wazai.dto.WazaiMapItem.Country;
import static dev.koukeneko.wazai.dto.WazaiMapItem.DataSource;

/**
 * Provider for GDG (Google Developer Groups) Community events.
 *
 * Integrates with the official GDG Community API at https://gdg.community.dev/api/search/
 * to fetch real-time event data from Google Developer Groups worldwide.
 *
 * API Documentation: https://gdg.community.dev/api/search/
 * - Supports proximity-based search
 * - Returns upcoming events
 * - Includes chapter information with geographic coordinates
 */
@Service
public class GdgCommunityProvider implements ActivityProvider {

    private static final String PROVIDER_NAME = "GDG Community";
    private static final String API_BASE_URL = "https://gdg.community.dev/api";
    private static final String SEARCH_ENDPOINT = "/search/";

    private static final int DEFAULT_PROXIMITY_KM = 10000;

    private final WebClient webClient;

    public GdgCommunityProvider(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(API_BASE_URL)
                .build();
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
                .map(this::transformToWazaiEvent)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
                    determineCountry(gdgEvent.chapter())
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

    private Coordinates extractCoordinates(GdgEvent event) {
        GdgChapter chapter = event.chapter();
        if (chapter == null) {
            return Coordinates.taipei();
        }

        String city = chapter.city();
        if (city == null) {
            return Coordinates.taipei();
        }

        return switch (city.toLowerCase()) {
            case "taipei" -> Coordinates.taipei();
            case "taichung" -> Coordinates.taichung();
            case "kaohsiung" -> Coordinates.kaohsiung();
            case "tokyo" -> Coordinates.tokyo();
            default -> Coordinates.taipei();
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

    private Country determineCountry(GdgChapter chapter) {
        if (chapter == null || chapter.country() == null) {
            return Country.TAIWAN;
        }

        String countryCode = chapter.country().toUpperCase();
        return switch (countryCode) {
            case "JP" -> Country.JAPAN;
            case "TW" -> Country.TAIWAN;
            default -> Country.TAIWAN;
        };
    }
}
