package dev.koukeneko.wazai.service.impl;

import dev.koukeneko.wazai.dto.Coordinates;
import dev.koukeneko.wazai.dto.WazaiEvent;
import dev.koukeneko.wazai.dto.WazaiEvent.EventType;
import dev.koukeneko.wazai.dto.WazaiMapItem;
import dev.koukeneko.wazai.dto.WazaiMapItem.DataSource;
import dev.koukeneko.wazai.dto.WazaiMapItem.Country;
import dev.koukeneko.wazai.dto.external.meetup.*;
import dev.koukeneko.wazai.service.ActivityProvider;
import dev.koukeneko.wazai.util.SearchHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MeetupProvider implements ActivityProvider {

    private static final String PROVIDER_NAME = "Meetup";
    private static final String API_URL = "https://www.meetup.com/gql2";
    
    // Default search center (Tokyo)
    private static final double DEFAULT_LAT = 35.6895;
    private static final double DEFAULT_LON = 139.6917;

    // Using keywordSearch query structure inferred from common Meetup GQL usage
    private static final String GQL_QUERY = """
        query keywordSearch($query: String!, $lat: Float!, $lon: Float!) {
          keywordSearch(filter: { query: $query, lat: $lat, lon: $lon, source: EVENTS }) {
            edges {
              node {
                id
                title
                shortDescription
                eventUrl
                dateTime
                venue {
                  name
                  address
                  city
                  lat
                  lon
                }
                group {
                  name
                }
              }
            }
          }
        }
    """;

    private final WebClient webClient;

    public MeetupProvider(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public List<WazaiMapItem> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            // Meetup keyword search requires a query string. 
            // We skip fetching if no keyword is provided to avoid errors or irrelevant default results.
            return Collections.emptyList();
        }

        return fetchEvents(keyword, DEFAULT_LAT, DEFAULT_LON);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    private List<WazaiMapItem> fetchEvents(String keyword, double lat, double lon) {
        try {
            Map<String, Object> variables = Map.of(
                "query", keyword,
                "lat", lat,
                "lon", lon
            );

            MeetupGqlRequest request = new MeetupGqlRequest(GQL_QUERY, variables);

            MeetupGqlResponse response = webClient.post()
                    .uri(API_URL)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(MeetupGqlResponse.class)
                    .block();

            if (response == null || response.data() == null || 
                response.data().keywordSearch() == null || 
                response.data().keywordSearch().edges() == null) {
                return Collections.emptyList();
            }

            return response.data().keywordSearch().edges().stream()
                    .map(MeetupEdge::node)
                    .map(this::transformEvent)
                    .filter(Objects::nonNull)
                    .filter(item -> SearchHelper.matchesKeyword(item, keyword))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("[Meetup] Error fetching events: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private WazaiEvent transformEvent(MeetupEvent event) {
        try {
            Coordinates coords;

            if (event.venue() != null && event.venue().lat() != null && event.venue().lon() != null) {
                coords = new Coordinates(event.venue().lat(), event.venue().lon());
            } else {
                // Skip events without location coordinates
                return null;
            }

            return new WazaiEvent(
                    "meetup-" + event.id(),
                    event.title(),
                    event.shortDescription() != null ? event.shortDescription() : event.group().name(),
                    event.eventUrl(),
                    coords,
                    buildVenueAddress(event),
                    parseTime(event.dateTime()),
                    null,
                    EventType.TECH_MEETUP,
                    DataSource.MEETUP,
                    Country.JAPAN
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String buildVenueAddress(MeetupEvent event) {
        if (event.venue() == null) {
            return null;
        }
        StringBuilder address = new StringBuilder();
        if (event.venue().name() != null && !event.venue().name().isBlank()) {
            address.append(event.venue().name());
        }
        if (event.venue().address() != null && !event.venue().address().isBlank()) {
            if (!address.isEmpty()) {
                address.append(" / ");
            }
            address.append(event.venue().address());
        }
        if (event.venue().city() != null && !event.venue().city().isBlank()) {
            if (!address.isEmpty()) {
                address.append(", ");
            }
            address.append(event.venue().city());
        }
        return address.isEmpty() ? null : address.toString();
    }

    private java.time.LocalDateTime parseTime(String isoTime) {
        try {
            if (isoTime == null) return null;
            return OffsetDateTime.parse(isoTime).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }
}
