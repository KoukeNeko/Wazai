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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provider for TechPlay events.
 *
 * TechPlay is a Japanese IT event aggregation service that provides
 * event information via RSS feed in W3C RSS format.
 *
 * For offline events, coordinates are extracted from the address field
 * using a mapping of Japanese prefectures and major areas.
 */
@Service
public class TechPlayProvider implements ActivityProvider {

    private static final String PROVIDER_NAME = "TechPlay";
    private static final String RSS_URL = "https://rss.techplay.jp/event/w3c-rss-format/rss.xml";
    private static final String ONLINE_INDICATOR = "オンライン";

    private static final Map<String, Coordinates> JAPAN_AREA_COORDINATES = createJapanAreaCoordinates();

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
                    extractCoordinates(item),
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
        if (item.description() != null && !item.description().isBlank()) {
            String desc = normalizeWhitespace(item.description());
            return desc.length() > 300 ? desc.substring(0, 300) + "..." : desc;
        }

        // Fallback: build from place/creator if no description
        StringBuilder fallback = new StringBuilder();

        if (item.eventPlace() != null && !item.eventPlace().isBlank()) {
            fallback.append(item.eventPlace());
        }

        if (item.creator() != null && !item.creator().isBlank()) {
            if (!fallback.isEmpty()) {
                fallback.append(" / ");
            }
            fallback.append("主催: ").append(item.creator());
        }

        return fallback.toString();
    }

    private String normalizeWhitespace(String text) {
        return text
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Extracts coordinates from the event address.
     * Uses a mapping of Japanese prefectures/areas to approximate location.
     * Returns Tokyo coordinates as default for online events or unknown addresses.
     */
    private Coordinates extractCoordinates(TechPlayItem item) {
        String address = item.eventAddress();
        String place = item.eventPlace();

        if (isOnlineEvent(place, address)) {
            return Coordinates.tokyo();
        }

        String locationText = combineLocationText(address, place);
        if (locationText.isEmpty()) {
            return Coordinates.tokyo();
        }

        for (Map.Entry<String, Coordinates> entry : JAPAN_AREA_COORDINATES.entrySet()) {
            if (locationText.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return Coordinates.tokyo();
    }

    private boolean isOnlineEvent(String place, String address) {
        if (place != null && place.contains(ONLINE_INDICATOR)) {
            return true;
        }
        if (address != null && address.contains(ONLINE_INDICATOR)) {
            return true;
        }
        return (place == null || place.isBlank()) && (address == null || address.isBlank());
    }

    private String combineLocationText(String address, String place) {
        StringBuilder sb = new StringBuilder();
        if (address != null && !address.isBlank()) {
            sb.append(address);
        }
        if (place != null && !place.isBlank()) {
            sb.append(place);
        }
        return sb.toString();
    }

    private LocalDateTime parseStartTime(TechPlayItem item) {
        String startTime = item.eventStartTime();
        if (startTime != null && !startTime.isBlank()) {
            return parseDateTime(startTime);
        }
        return null;
    }

    private LocalDateTime parseEndTime(TechPlayItem item) {
        String endTime = item.eventEndTime();
        if (endTime != null && !endTime.isBlank()) {
            return parseDateTime(endTime);
        }
        return null;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            // TechPlay format: "2025-12-09 13:00:00"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (DateTimeParseException e) {
            try {
                // Fallback: ISO format
                return LocalDateTime.parse(dateTimeStr);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    /**
     * Creates a mapping of Japanese prefectures and major areas to coordinates.
     * Ordered by specificity (wards before prefectures) for correct matching.
     */
    private static Map<String, Coordinates> createJapanAreaCoordinates() {
        Map<String, Coordinates> map = new HashMap<>();

        // Tokyo Wards (23区) - more specific matching
        map.put("渋谷区", new Coordinates(35.6640, 139.6982));
        map.put("新宿区", new Coordinates(35.6938, 139.7034));
        map.put("港区", new Coordinates(35.6581, 139.7514));
        map.put("千代田区", new Coordinates(35.6940, 139.7536));
        map.put("中央区", new Coordinates(35.6707, 139.7720));
        map.put("品川区", new Coordinates(35.6092, 139.7302));
        map.put("目黒区", new Coordinates(35.6413, 139.6983));
        map.put("世田谷区", new Coordinates(35.6463, 139.6532));
        map.put("大田区", new Coordinates(35.5613, 139.7160));
        map.put("江東区", new Coordinates(35.6729, 139.8172));
        map.put("墨田区", new Coordinates(35.7126, 139.8107));
        map.put("台東区", new Coordinates(35.7125, 139.7800));
        map.put("文京区", new Coordinates(35.7081, 139.7522));
        map.put("豊島区", new Coordinates(35.7263, 139.7163));
        map.put("北区", new Coordinates(35.7528, 139.7373));
        map.put("荒川区", new Coordinates(35.7365, 139.7834));
        map.put("板橋区", new Coordinates(35.7514, 139.7097));
        map.put("練馬区", new Coordinates(35.7355, 139.6517));
        map.put("足立区", new Coordinates(35.7752, 139.8045));
        map.put("葛飾区", new Coordinates(35.7436, 139.8477));
        map.put("江戸川区", new Coordinates(35.7067, 139.8683));
        map.put("中野区", new Coordinates(35.7078, 139.6638));
        map.put("杉並区", new Coordinates(35.6994, 139.6364));

        // Major Cities
        map.put("横浜", new Coordinates(35.4437, 139.6380));
        map.put("大阪", new Coordinates(34.6937, 135.5023));
        map.put("名古屋", new Coordinates(35.1815, 136.9066));
        map.put("札幌", new Coordinates(43.0618, 141.3545));
        map.put("福岡", new Coordinates(33.5902, 130.4017));
        map.put("神戸", new Coordinates(34.6901, 135.1956));
        map.put("京都", new Coordinates(35.0116, 135.7681));
        map.put("仙台", new Coordinates(38.2682, 140.8694));
        map.put("広島", new Coordinates(34.3853, 132.4553));
        map.put("さいたま", new Coordinates(35.8617, 139.6455));
        map.put("千葉", new Coordinates(35.6073, 140.1063));

        // Prefectures
        map.put("東京都", new Coordinates(35.6812, 139.7671));
        map.put("神奈川県", new Coordinates(35.4478, 139.6425));
        map.put("埼玉県", new Coordinates(35.8569, 139.6489));
        map.put("千葉県", new Coordinates(35.6050, 140.1233));
        map.put("大阪府", new Coordinates(34.6864, 135.5200));
        map.put("愛知県", new Coordinates(35.1802, 136.9066));
        map.put("北海道", new Coordinates(43.0646, 141.3468));
        map.put("福岡県", new Coordinates(33.6064, 130.4180));
        map.put("兵庫県", new Coordinates(34.6913, 135.1830));
        map.put("京都府", new Coordinates(35.0214, 135.7556));
        map.put("宮城県", new Coordinates(38.2688, 140.8721));
        map.put("広島県", new Coordinates(34.3966, 132.4596));
        map.put("静岡県", new Coordinates(34.9769, 138.3831));
        map.put("岡山県", new Coordinates(34.6618, 133.9344));
        map.put("茨城県", new Coordinates(36.3414, 140.4467));
        map.put("新潟県", new Coordinates(37.9026, 139.0236));
        map.put("長野県", new Coordinates(36.6513, 138.1810));
        map.put("石川県", new Coordinates(36.5947, 136.6256));
        map.put("沖縄県", new Coordinates(26.2124, 127.6809));

        return map;
    }
}
