package dev.koukeneko.wazai.service.impl;

import dev.koukeneko.wazai.dto.Coordinates;
import dev.koukeneko.wazai.dto.WazaiEvent;
import dev.koukeneko.wazai.dto.WazaiEvent.EventType;
import dev.koukeneko.wazai.dto.WazaiMapItem;
import dev.koukeneko.wazai.dto.WazaiMapItem.Country;
import dev.koukeneko.wazai.dto.WazaiMapItem.DataSource;
import dev.koukeneko.wazai.dto.external.doorkeeper.DoorkeeperEvent;
import dev.koukeneko.wazai.dto.external.doorkeeper.DoorkeeperEventWrapper;
import dev.koukeneko.wazai.service.ActivityProvider;
import dev.koukeneko.wazai.util.SearchHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provider for Doorkeeper events.
 * Doorkeeper is a popular event management platform in Japan.
 *
 * @see <a href="https://www.doorkeeper.jp/developer/api">Doorkeeper API Documentation</a>
 */
@Service
public class DoorkeeperProvider implements ActivityProvider {

    private static final String PROVIDER_NAME = "Doorkeeper";
    private static final String BASE_URL = "https://api.doorkeeper.jp";
    private static final int PAGES_TO_FETCH = 4;
    private static final int RESULTS_PER_PAGE = 25;

    @Value("${doorkeeper.api.token:}")
    private String apiToken;

    private final RestClient restClient;

    public DoorkeeperProvider(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    @Override
    public List<WazaiMapItem> search(String keyword) {
        if (isTokenMissing()) {
            System.err.println("[Doorkeeper] API token not configured");
            return Collections.emptyList();
        }

        if (isEmptyKeyword(keyword)) {
            return fetchEvents(null);
        }

        // First try API search, then filter locally for more results
        List<WazaiMapItem> apiResults = fetchEvents(keyword);
        if (!apiResults.isEmpty()) {
            return apiResults;
        }

        // Fallback to local filtering if API search returns nothing
        return fetchEvents(null).stream()
                .filter(item -> SearchHelper.matchesKeyword(item, keyword))
                .collect(Collectors.toList());
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    private boolean isTokenMissing() {
        return apiToken == null || apiToken.isBlank();
    }

    private boolean isEmptyKeyword(String keyword) {
        return keyword == null || keyword.isBlank();
    }

    private List<WazaiMapItem> fetchEvents(String keyword) {
        List<WazaiMapItem> allEvents = new ArrayList<>();

        for (int page = 1; page <= PAGES_TO_FETCH; page++) {
            try {
                List<DoorkeeperEventWrapper> pageEvents = fetchEventsPage(page, keyword);
                if (pageEvents == null || pageEvents.isEmpty()) {
                    break;
                }

                List<WazaiMapItem> transformed = pageEvents.stream()
                        .map(DoorkeeperEventWrapper::event)
                        .filter(this::isValidEvent)
                        .<WazaiMapItem>map(this::transformEvent)
                        .toList();

                allEvents.addAll(transformed);

                if (pageEvents.size() < RESULTS_PER_PAGE) {
                    break;
                }
            } catch (Exception e) {
                System.err.println("[Doorkeeper] Error fetching page " + page + ": " + e.getMessage());
                break;
            }
        }

        return allEvents;
    }

    private List<DoorkeeperEventWrapper> fetchEventsPage(int page, String keyword) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/events")
                            .queryParam("page", page)
                            .queryParam("sort", "published_at")
                            .queryParam("locale", "ja");
                    if (keyword != null && !keyword.isBlank()) {
                        uriBuilder.queryParam("q", keyword);
                    }
                    return uriBuilder.build();
                })
                .header("Authorization", "Bearer " + apiToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    private boolean isValidEvent(DoorkeeperEvent event) {
        return event != null && event.title() != null && !event.title().isBlank();
    }

    private WazaiEvent transformEvent(DoorkeeperEvent event) {
        return new WazaiEvent(
                generateEventId(event.id()),
                event.title(),
                extractDescription(event),
                event.publicUrl(),
                extractCoordinates(event),
                buildAddress(event),
                parseDateTime(event.startsAt()),
                parseDateTime(event.endsAt()),
                EventType.TECH_MEETUP,
                DataSource.DOORKEEPER,
                Country.JAPAN
        );
    }

    private String generateEventId(long eventId) {
        return "doorkeeper-" + eventId;
    }

    private String extractDescription(DoorkeeperEvent event) {
        if (event.description() != null && !event.description().isBlank()) {
            String plainText = event.description()
                    .replaceAll("<[^>]*>", "")
                    .replaceAll("\\s+", " ")
                    .trim();
            return plainText.length() > 300 ? plainText.substring(0, 300) + "..." : plainText;
        }
        return buildLocationDescription(event);
    }

    private String buildLocationDescription(DoorkeeperEvent event) {
        StringBuilder description = new StringBuilder();
        if (event.venueName() != null && !event.venueName().isBlank()) {
            description.append(event.venueName());
        }
        if (event.address() != null && !event.address().isBlank()) {
            if (!description.isEmpty()) {
                description.append(" - ");
            }
            description.append(event.address());
        }
        return description.toString();
    }

    private String buildAddress(DoorkeeperEvent event) {
        StringBuilder address = new StringBuilder();
        if (event.venueName() != null && !event.venueName().isBlank()) {
            address.append(event.venueName());
        }
        if (event.address() != null && !event.address().isBlank()) {
            if (!address.isEmpty()) {
                address.append(" / ");
            }
            address.append(event.address());
        }
        return address.isEmpty() ? null : address.toString();
    }

    private Coordinates extractCoordinates(DoorkeeperEvent event) {
        if (event.lat() != null && event.lng() != null) {
            try {
                double latitude = Double.parseDouble(event.lat());
                double longitude = Double.parseDouble(event.lng());
                if (isValidCoordinate(latitude, longitude)) {
                    return new Coordinates(latitude, longitude);
                }
            } catch (NumberFormatException e) {
                // Fall through to default
            }
        }
        return Coordinates.tokyo();
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }

        try {
            ZonedDateTime zdt = ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return zdt.toLocalDateTime();
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}
