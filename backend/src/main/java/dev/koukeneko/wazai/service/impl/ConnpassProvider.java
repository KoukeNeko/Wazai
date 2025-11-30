package dev.koukeneko.wazai.service.impl;

import dev.koukeneko.wazai.dto.Coordinates;
import dev.koukeneko.wazai.dto.WazaiEvent;
import dev.koukeneko.wazai.dto.WazaiEvent.EventType;
import dev.koukeneko.wazai.dto.WazaiMapItem;
import dev.koukeneko.wazai.dto.WazaiMapItem.DataSource;
import dev.koukeneko.wazai.dto.WazaiMapItem.Country;
import dev.koukeneko.wazai.dto.external.connpass.ConnpassEvent;
import dev.koukeneko.wazai.dto.external.connpass.ConnpassResponse;
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

    @Value("${connpass.api.token:}")
    private String apiToken;

    private final RestClient restClient;

    public ConnpassProvider(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    @Override
    public List<WazaiMapItem> search(String keyword) {
        ConnpassResponse response = fetchConnpassEvents(keyword);

        if (isEmptyResponse(response)) {
            return Collections.emptyList();
        }

        return transformToWazaiEvents(response.events());
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

    private List<WazaiMapItem> transformToWazaiEvents(List<ConnpassEvent> events) {
        return events.stream()
                .<WazaiMapItem>map(this::transformEvent)
                .toList();
    }

    private WazaiEvent transformEvent(ConnpassEvent event) {
        return new WazaiEvent(
                generateActivityId(event.event_id()),
                event.title(),
                extractDescription(event),
                event.event_url(),
                Coordinates.tokyo(),  // TODO: Extract from event.address or event.place
                parseStartTime(event.started_at()),
                EventType.TECH_MEETUP,
                DataSource.CONNPASS,
                Country.JAPAN
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
