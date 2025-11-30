package dev.koukeneko.wazai.service.impl;

import dev.koukeneko.wazai.dto.WazaiActivity;
import dev.koukeneko.wazai.dto.WazaiActivity.ActivityType;
import dev.koukeneko.wazai.dto.external.ConnpassEvent;
import dev.koukeneko.wazai.dto.external.ConnpassResponse;
import dev.koukeneko.wazai.service.ActivityProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

/**
 * Connpass API provider implementation.
 * This service fetches tech events from Connpass API and transforms them
 * into the unified WazaiActivity format.
 */
@Service
public class ConnpassProvider implements ActivityProvider {

    private static final String PROVIDER_NAME = "Connpass";
    private static final String BASE_URL = "https://connpass.com/api/v2";
    private static final String API_PATH = "/event/";
    private static final int DEFAULT_RESULT_COUNT = 10;
    private static final double DEFAULT_LATITUDE = 0.0;
    private static final double DEFAULT_LONGITUDE = 0.0;

    @Value("${connpass.api.token:}")
    private String apiToken;

    private final RestClient restClient;

    public ConnpassProvider(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    @Override
    public List<WazaiActivity> search(String keyword) {
        ConnpassResponse response = fetchConnpassEvents(keyword);

        if (isEmptyResponse(response)) {
            return Collections.emptyList();
        }

        return transformToWazaiActivities(response.events());
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    private ConnpassResponse fetchConnpassEvents(String keyword) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(API_PATH)
                        .queryParam("keyword", keyword)
                        .queryParam("count", DEFAULT_RESULT_COUNT)
                        .build())
                .header("Authorization", "Bearer " + apiToken)
                .retrieve()
                .body(ConnpassResponse.class);
    }

    private boolean isEmptyResponse(ConnpassResponse response) {
        return response == null || response.events() == null || response.events().isEmpty();
    }

    private List<WazaiActivity> transformToWazaiActivities(List<ConnpassEvent> events) {
        return events.stream()
                .map(this::transformEvent)
                .toList();
    }

    private WazaiActivity transformEvent(ConnpassEvent event) {
        return new WazaiActivity(
                generateActivityId(event.event_id()),
                event.title(),
                extractDescription(event),
                event.event_url(),
                DEFAULT_LATITUDE,  // TODO: Extract from event.address or event.place
                DEFAULT_LONGITUDE, // TODO: Extract from event.address or event.place
                parseStartTime(event.started_at()),
                ActivityType.TECH_EVENT,
                PROVIDER_NAME
        );
    }

    private String generateActivityId(long eventId) {
        return "connpass-" + eventId;
    }

    private String extractDescription(ConnpassEvent event) {
        if (event.catchMessage() != null && !event.catchMessage().isBlank()) {
            return event.catchMessage();
        }
        return buildLocationDescription(event);
    }

    private String buildLocationDescription(ConnpassEvent event) {
        StringBuilder description = new StringBuilder();
        if (event.place() != null && !event.place().isBlank()) {
            description.append(event.place());
        }
        if (event.address() != null && !event.address().isBlank()) {
            if (description.length() > 0) {
                description.append(" - ");
            }
            description.append(event.address());
        }
        return description.toString();
    }

    private LocalDateTime parseStartTime(String startedAt) {
        if (startedAt == null || startedAt.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(startedAt, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
