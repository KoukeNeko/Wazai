package dev.koukeneko.wazai.service.impl;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dev.koukeneko.wazai.dto.Coordinates;
import dev.koukeneko.wazai.dto.WazaiEvent;
import dev.koukeneko.wazai.dto.WazaiEvent.EventType;
import dev.koukeneko.wazai.dto.WazaiMapItem;
import dev.koukeneko.wazai.dto.WazaiMapItem.Country;
import dev.koukeneko.wazai.dto.WazaiMapItem.DataSource;
import dev.koukeneko.wazai.dto.external.techplay.TechPlayItem;
import dev.koukeneko.wazai.dto.external.techplay.TechPlayRss;
import dev.koukeneko.wazai.service.ActivityProvider;
import dev.koukeneko.wazai.util.SearchHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provider for TechPlay events.
 *
 * TechPlay is a Japanese IT event aggregation service that provides
 * event information via RSS feed in W3C RSS format.
 *
 * Note: The RSS feed does not include geographic coordinates,
 * so Tokyo coordinates are used as the default location.
 */
@Service
public class TechPlayProvider implements ActivityProvider {

    private static final String PROVIDER_NAME = "TechPlay";
    private static final String RSS_URL = "https://rss.techplay.jp/event/w3c-rss-format/rss.xml";

    private final WebClient webClient;
    private final XmlMapper xmlMapper;

    public TechPlayProvider(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.xmlMapper = new XmlMapper();
    }

    @Override
    public List<WazaiMapItem> search(String keyword) {
        List<WazaiMapItem> allEvents = fetchEvents();

        if (isEmptyKeyword(keyword)) {
            return allEvents;
        }

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

    private List<WazaiMapItem> fetchEvents() {
        try {
            String rssXml = webClient.get()
                    .uri(RSS_URL)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (rssXml == null || rssXml.isBlank()) {
                return Collections.emptyList();
            }

            TechPlayRss rss = xmlMapper.readValue(rssXml, TechPlayRss.class);
            if (rss == null || rss.channel() == null || rss.channel().items() == null) {
                return Collections.emptyList();
            }

            return transformEvents(rss.channel().items());

        } catch (Exception e) {
            System.err.println("[TechPlay] Error fetching events: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<WazaiMapItem> transformEvents(List<TechPlayItem> items) {
        return items.stream()
                .map(this::transformEvent)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private WazaiEvent transformEvent(TechPlayItem item) {
        try {
            return new WazaiEvent(
                    generateEventId(item),
                    item.title(),
                    buildDescription(item),
                    item.link(),
                    Coordinates.tokyo(),
                    parseStartTime(item),
                    parseEndTime(item),
                    EventType.TECH_MEETUP,
                    DataSource.TECHPLAY,
                    Country.JAPAN
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String generateEventId(TechPlayItem item) {
        return "techplay-" + item.extractEventId();
    }

    private String buildDescription(TechPlayItem item) {
        StringBuilder description = new StringBuilder();

        if (item.eventPlace() != null && !item.eventPlace().isBlank()) {
            description.append(item.eventPlace());
        }

        if (item.creator() != null && !item.creator().isBlank()) {
            if (!description.isEmpty()) {
                description.append(" - ");
            }
            description.append("主催: ").append(item.creator());
        }

        if (description.isEmpty() && item.description() != null) {
            String desc = item.description();
            return desc.length() > 200 ? desc.substring(0, 200) + "..." : desc;
        }

        return description.toString();
    }

    private LocalDateTime parseStartTime(TechPlayItem item) {
        String startTime = item.eventStartTime();
        if (startTime != null && !startTime.isBlank()) {
            return parseIsoDateTime(startTime);
        }
        return null;
    }

    private LocalDateTime parseEndTime(TechPlayItem item) {
        String endTime = item.eventEndTime();
        if (endTime != null && !endTime.isBlank()) {
            return parseIsoDateTime(endTime);
        }
        return null;
    }

    private LocalDateTime parseIsoDateTime(String dateTimeStr) {
        try {
            return OffsetDateTime.parse(dateTimeStr).toLocalDateTime();
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateTimeStr);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}
