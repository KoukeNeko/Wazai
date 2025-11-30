package dev.koukeneko.wazai.service.impl;

import dev.koukeneko.wazai.dto.Coordinates;
import dev.koukeneko.wazai.dto.WazaiEvent;
import dev.koukeneko.wazai.dto.WazaiMapItem;
import dev.koukeneko.wazai.dto.external.aws.AwsApiResponse;
import dev.koukeneko.wazai.dto.external.aws.AwsEventWrapper;
import dev.koukeneko.wazai.dto.external.aws.AwsAdditionalFields;
import dev.koukeneko.wazai.service.ActivityProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import dev.koukeneko.wazai.util.SearchHelper;

import static dev.koukeneko.wazai.dto.WazaiEvent.EventType;
import static dev.koukeneko.wazai.dto.WazaiMapItem.Country;
import static dev.koukeneko.wazai.dto.WazaiMapItem.DataSource;

/**
 * Provider for AWS events (Summit and Community Day).
 *
 * Integrates with the AWS official events API to fetch upcoming AWS events worldwide.
 * Supports two event types:
 * - AWS Summit: Large-scale official AWS conferences
 * - AWS Community Day: Community-organized AWS events
 */
@Service
public class AwsSummitProvider implements ActivityProvider {

    private static final String PROVIDER_NAME = "AWS Events";
    private static final String API_BASE_URL = "https://aws.amazon.com";
    private static final String API_ENDPOINT = "/api/dirs/items/search";

    // AWS Summit configuration
    private static final String SUMMIT_DIRECTORY_ID = "events-cards-interactive-summits-cards-interactive-events-summits-hub-interactive-cards1";

    // AWS Community Day configuration
    private static final String COMMUNITY_DIRECTORY_ID = "developer-cards-interactive-dev-center-activities";
    private static final String COMMUNITY_TAG_ID = "GLOBAL#local-tags-series#aws-community-days";

    private static final String LOCALE_ZH_TW = "zh_TW";
    private static final String LOCALE_EN_US = "en_US";
    private static final int DEFAULT_PAGE_SIZE = 50;

    private static final Map<String, Coordinates> CITY_COORDINATES = createCityCoordinatesMap();
    private static final Coordinates DEFAULT_COORDINATES = new Coordinates(40.7128, -74.0060); // New York as default

    private final WebClient webClient;

    public AwsSummitProvider(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(API_BASE_URL)
                .build();
    }

    @Override
    public List<WazaiMapItem> search(String keyword) {
        if (isEmptyKeyword(keyword)) {
            return Collections.emptyList();
        }

        List<WazaiMapItem> allEvents = new ArrayList<>();
        allEvents.addAll(fetchAwsSummitEvents());
        allEvents.addAll(fetchAwsCommunityDayEvents());

        return allEvents.stream()
                .filter(item -> SearchHelper.matchesKeyword(item, keyword))
                .collect(Collectors.toList());
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    private boolean isEmptyKeyword(String keyword) {
        return keyword == null || keyword.isBlank();
    }

    private List<WazaiMapItem> fetchAwsSummitEvents() {
        try {
            System.out.println("[AWS] Fetching AWS Summit events from API...");

            AwsApiResponse response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(API_ENDPOINT)
                            .queryParam("item.directoryId", SUMMIT_DIRECTORY_ID)
                            .queryParam("item.locale", LOCALE_ZH_TW)
                            .queryParam("sort_by", "item.additionalFields.publishedDate")
                            .queryParam("sort_order", "asc")
                            .queryParam("size", DEFAULT_PAGE_SIZE)
                            .build())
                    .retrieve()
                    .bodyToMono(AwsApiResponse.class)
                    .block();

            if (response == null || response.items() == null || response.items().isEmpty()) {
                System.out.println("[AWS] No AWS Summit events found");
                return Collections.emptyList();
            }

            System.out.println("[AWS] Loaded " + response.items().size() + " AWS Summit events");

            return transformAwsEvents(response.items(), EventType.TECH_CONFERENCE);

        } catch (Exception e) {
            System.err.println("[AWS] Failed to fetch Summit events: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<WazaiMapItem> fetchAwsCommunityDayEvents() {
        try {
            System.out.println("[AWS] Fetching AWS Community Day events from API...");

            AwsApiResponse response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(API_ENDPOINT)
                            .queryParam("item.directoryId", COMMUNITY_DIRECTORY_ID)
                            .queryParam("item.locale", LOCALE_EN_US)
                            .queryParam("tags.id", COMMUNITY_TAG_ID)
                            .queryParam("sort_by", "item.additionalFields.publishedDate")
                            .queryParam("sort_order", "asc")
                            .queryParam("size", DEFAULT_PAGE_SIZE)
                            .build())
                    .retrieve()
                    .bodyToMono(AwsApiResponse.class)
                    .block();

            if (response == null || response.items() == null || response.items().isEmpty()) {
                System.out.println("[AWS] No AWS Community Day events found");
                return Collections.emptyList();
            }

            System.out.println("[AWS] Loaded " + response.items().size() + " AWS Community Day events");

            return transformAwsEvents(response.items(), EventType.COMMUNITY_GATHERING);

        } catch (Exception e) {
            System.err.println("[AWS] Failed to fetch Community Day events: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<WazaiMapItem> transformAwsEvents(List<AwsEventWrapper> events, EventType eventType) {
        return events.stream()
                // Temporarily disabled for testing - re-enable to filter past events
                // .filter(this::isUpcomingEvent)
                .map(wrapper -> transformToWazaiEvent(wrapper, eventType))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean isUpcomingEvent(AwsEventWrapper wrapper) {
        if (wrapper.item() == null || wrapper.item().additionalFields() == null) {
            return false;
        }

        String dateStr = wrapper.item().additionalFields().date();
        if (dateStr == null || dateStr.isBlank()) {
            return false;
        }

        try {
            LocalDate eventDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return !eventDate.isBefore(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private WazaiEvent transformToWazaiEvent(AwsEventWrapper wrapper, EventType eventType) {
        try {
            AwsAdditionalFields fields = wrapper.item().additionalFields();

            return new WazaiEvent(
                    generateEventId(wrapper.item().id()),
                    extractTitle(fields),
                    extractDescription(fields),
                    extractEventUrl(fields),
                    extractCoordinatesFromFields(fields),
                    parseEventDateTime(fields),
                    null,
                    eventType,
                    DataSource.AWS_EVENTS,
                    determineCountry(fields)
            );
        } catch (Exception e) {
            System.err.println("[AWS] Failed to transform event: " + e.getMessage());
            return null;
        }
    }

    private String generateEventId(String awsEventId) {
        return "aws-summit-" + awsEventId.hashCode();
    }

    private String extractTitle(AwsAdditionalFields fields) {
        String title = fields.title();
        if (title == null || title.isBlank()) {
            return fields.heading();
        }
        return title.trim();
    }

    private String extractDescription(AwsAdditionalFields fields) {
        String desc = fields.body();
        if (desc == null || desc.isBlank()) {
            return "AWS Summit - Official AWS event";
        }
        return desc.length() > 500 ? desc.substring(0, 500) + "..." : desc;
    }

    private String extractEventUrl(AwsAdditionalFields fields) {
        String url = fields.ctaLink();
        return url != null && !url.isBlank() ? url : "https://aws.amazon.com/events/summits/";
    }

    private Coordinates extractCoordinatesFromFields(AwsAdditionalFields fields) {
        // First try location field (Community Day events)
        String location = fields.location();
        if (location != null && !location.isBlank()) {
            for (Map.Entry<String, Coordinates> entry : CITY_COORDINATES.entrySet()) {
                if (location.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        // Fallback to title field (Summit events)
        String title = fields.title();
        if (title != null) {
            for (Map.Entry<String, Coordinates> entry : CITY_COORDINATES.entrySet()) {
                if (title.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        return DEFAULT_COORDINATES;
    }

    private LocalDateTime parseEventDateTime(AwsAdditionalFields fields) {
        String dateStr = fields.date();
        String timeStr = fields.time();

        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);

            if (timeStr != null && !timeStr.isBlank()) {
                try {
                    // Parse time with timezone offset (e.g., "15:00+00:00")
                    String cleanTime = timeStr.split("\\+")[0].split("-")[0];
                    LocalTime time = LocalTime.parse(cleanTime, DateTimeFormatter.ofPattern("HH:mm"));
                    return LocalDateTime.of(date, time);
                } catch (DateTimeParseException e) {
                    // If time parsing fails, use date only
                }
            }

            return date.atStartOfDay();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Country determineCountry(AwsAdditionalFields fields) {
        String title = fields.title();
        if (title == null) {
            return Country.DEFAULT;
        }

        // Check for known cities/countries
        if (title.contains("Taipei") || title.contains("Taiwan")) {
            return Country.TAIWAN;
        }
        if (title.contains("Tokyo") || title.contains("Osaka") || title.contains("Japan")) {
            return Country.JAPAN;
        }

        return Country.DEFAULT;
    }

    private static Map<String, Coordinates> createCityCoordinatesMap() {
        Map<String, Coordinates> map = new HashMap<>();

        // Americas
        map.put("New York", new Coordinates(40.7128, -74.0060));
        map.put("Los Angeles", new Coordinates(34.0522, -118.2437));
        map.put("San Francisco", new Coordinates(37.7749, -122.4194));
        map.put("Chicago", new Coordinates(41.8781, -87.6298));
        map.put("Toronto", new Coordinates(43.6532, -79.3832));
        map.put("Vancouver", new Coordinates(49.2827, -123.1207));
        map.put("Mexico City", new Coordinates(19.4326, -99.1332));
        map.put("Bogotá", new Coordinates(4.7110, -74.0721));
        map.put("São Paulo", new Coordinates(-23.5505, -46.6333));
        map.put("Quito", new Coordinates(-0.1807, -78.4678));

        // Europe
        map.put("London", new Coordinates(51.5074, -0.1278));
        map.put("Paris", new Coordinates(48.8566, 2.3522));
        map.put("Berlin", new Coordinates(52.5200, 13.4050));
        map.put("Amsterdam", new Coordinates(52.3676, 4.9041));
        map.put("Stockholm", new Coordinates(59.3293, 18.0686));
        map.put("Madrid", new Coordinates(40.4168, -3.7038));
        map.put("Milan", new Coordinates(45.4642, 9.1900));
        map.put("Zurich", new Coordinates(47.3769, 8.5417));
        map.put("Sofia", new Coordinates(42.6977, 23.3219));
        map.put("Zaragoza", new Coordinates(41.6488, -0.8891));

        // Asia Pacific
        map.put("Tokyo", new Coordinates(35.6762, 139.6503));
        map.put("Osaka", new Coordinates(34.6937, 135.5023));
        map.put("Singapore", new Coordinates(1.3521, 103.8198));
        map.put("Hong Kong", new Coordinates(22.3193, 114.1694));
        map.put("Seoul", new Coordinates(37.5665, 126.9780));
        map.put("Sydney", new Coordinates(-33.8688, 151.2093));
        map.put("Melbourne", new Coordinates(-37.8136, 144.9631));
        map.put("Mumbai", new Coordinates(19.0760, 72.8777));
        map.put("Bangkok", new Coordinates(13.7563, 100.5018));
        map.put("Taipei", new Coordinates(25.0330, 121.5654));

        // Africa
        map.put("Abuja", new Coordinates(9.0765, 7.3986));
        map.put("Kinshasa", new Coordinates(-4.4419, 15.2663));
        map.put("Buea", new Coordinates(4.1560, 9.2320));

        return map;
    }
}
