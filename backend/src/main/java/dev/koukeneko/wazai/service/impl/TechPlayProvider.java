package dev.koukeneko.wazai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.koukeneko.wazai.dto.Coordinates;
import dev.koukeneko.wazai.dto.WazaiEvent;
import dev.koukeneko.wazai.dto.WazaiEvent.EventType;
import dev.koukeneko.wazai.dto.WazaiMapItem;
import dev.koukeneko.wazai.dto.WazaiMapItem.Country;
import dev.koukeneko.wazai.dto.WazaiMapItem.DataSource;
import dev.koukeneko.wazai.service.ActivityProvider;
import dev.koukeneko.wazai.service.GeocodingService;
import dev.koukeneko.wazai.util.SearchHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Provider for TechPlay events.
 *
 * TechPlay is a Japanese IT event aggregation service.
 * This provider scrapes the event listing pages to get event URLs,
 * then fetches JSON-LD structured data from individual event pages.
 */
@Service
public class TechPlayProvider implements ActivityProvider {

    private static final String PROVIDER_NAME = "TechPlay";
    private static final String EVENT_LIST_URL = "https://techplay.jp/event";
    private static final String ONLINE_INDICATOR = "オンライン";
    private static final int PAGES_TO_FETCH = 10;
    private static final int CONNECTION_TIMEOUT_MS = 10000;

    private static final Map<String, Coordinates> JAPAN_AREA_COORDINATES = createJapanAreaCoordinates();

    private final ObjectMapper objectMapper;
    private final GeocodingService geocodingService;

    public TechPlayProvider(GeocodingService geocodingService) {
        this.objectMapper = new ObjectMapper();
        this.geocodingService = geocodingService;
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
        List<String> eventUrls = collectEventUrlsFromPages();
        return fetchEventDetails(eventUrls);
    }

    /**
     * Collects event URLs from multiple listing pages.
     */
    private List<String> collectEventUrlsFromPages() {
        List<String> urls = new ArrayList<>();

        for (int page = 1; page <= PAGES_TO_FETCH; page++) {
            try {
                List<String> pageUrls = scrapeEventUrlsFromPage(page);
                urls.addAll(pageUrls);
            } catch (IOException e) {
                System.err.println("[TechPlay] Error fetching page " + page + ": " + e.getMessage());
            }
        }

        return urls;
    }

    /**
     * Scrapes event URLs from a single listing page.
     */
    private List<String> scrapeEventUrlsFromPage(int pageNumber) throws IOException {
        String url = EVENT_LIST_URL + "?page=" + pageNumber;

        Document doc = Jsoup.connect(url)
                .timeout(CONNECTION_TIMEOUT_MS)
                .userAgent("Mozilla/5.0 (compatible; WazaiBot/1.0)")
                .get();

        Elements links = doc.select("a[href^=https://techplay.jp/event/]");

        return links.stream()
                .map(link -> link.attr("href"))
                .filter(href -> href.matches("https://techplay.jp/event/\\d+"))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Fetches event details from individual event pages using JSON-LD.
     */
    private List<WazaiMapItem> fetchEventDetails(List<String> eventUrls) {
        Map<String, WazaiMapItem> eventMap = new ConcurrentHashMap<>();

        eventUrls.parallelStream().forEach(url -> {
            try {
                WazaiMapItem event = fetchSingleEventDetail(url);
                if (event != null) {
                    eventMap.put(event.id(), event);
                }
            } catch (Exception e) {
                System.err.println("[TechPlay] Error fetching event " + url + ": " + e.getMessage());
            }
        });

        return new ArrayList<>(eventMap.values());
    }

    /**
     * Fetches details for a single event from its page using JSON-LD structured data.
     */
    private WazaiMapItem fetchSingleEventDetail(String eventUrl) throws IOException {
        Document doc = Jsoup.connect(eventUrl)
                .timeout(CONNECTION_TIMEOUT_MS)
                .userAgent("Mozilla/5.0 (compatible; WazaiBot/1.0)")
                .get();

        Element jsonLdScript = doc.selectFirst("script[type=application/ld+json]");
        if (jsonLdScript == null) {
            return null;
        }

        String jsonLdContent = jsonLdScript.html();
        JsonNode jsonLd = objectMapper.readTree(jsonLdContent);

        return parseJsonLdEvent(jsonLd, eventUrl);
    }

    /**
     * Parses JSON-LD structured data into a WazaiMapItem.
     */
    private WazaiEvent parseJsonLdEvent(JsonNode jsonLd, String eventUrl) {
        String eventId = extractEventIdFromUrl(eventUrl);
        String title = getJsonText(jsonLd, "name");
        String description = getJsonText(jsonLd, "description");
        LocalDateTime startTime = parseJsonLdDateTime(getJsonText(jsonLd, "startDate"));
        LocalDateTime endTime = parseJsonLdDateTime(getJsonText(jsonLd, "endDate"));
        LocationInfo locationInfo = extractLocationInfo(jsonLd);

        if (title == null || title.isBlank()) {
            return null;
        }

        return new WazaiEvent(
                "techplay-" + eventId,
                title,
                normalizeDescription(description),
                eventUrl,
                locationInfo.coordinates(),
                locationInfo.address(),
                startTime,
                endTime,
                EventType.TECH_MEETUP,
                DataSource.TECHPLAY,
                Country.JAPAN
        );
    }

    private record LocationInfo(Coordinates coordinates, String address) {}

    private String extractEventIdFromUrl(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    private String getJsonText(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null && !field.isNull() ? field.asText() : null;
    }

    private LocalDateTime parseJsonLdDateTime(String dateTimeStr) {
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

    /**
     * Extracts location info (coordinates and address) from JSON-LD location data.
     * Priority: address (local mapping + Nominatim) -> venue name (fallback)
     * Address is prioritized because it's more specific than venue name which can be ambiguous.
     */
    private LocationInfo extractLocationInfo(JsonNode jsonLd) {
        JsonNode location = jsonLd.get("location");
        if (location == null) {
            return new LocationInfo(Coordinates.tokyo(), null);
        }

        // Check for VirtualLocation (online event)
        String locationType = getJsonText(location, "@type");
        if ("VirtualLocation".equals(locationType)) {
            return new LocationInfo(Coordinates.tokyo(), ONLINE_INDICATOR);
        }

        // Extract venue name and address
        String venueName = getJsonText(location, "name");
        String addressText = extractAddressText(location);
        String displayAddress = buildDisplayAddress(venueName, addressText);

        // Skip geocoding for online events
        if (venueName != null && venueName.contains(ONLINE_INDICATOR)) {
            return new LocationInfo(Coordinates.tokyo(), ONLINE_INDICATOR);
        }

        // First try address-based geocoding (more accurate than venue name)
        if (addressText != null && !addressText.isBlank()) {
            Coordinates coords = geocodeAddress(addressText);
            return new LocationInfo(coords, displayAddress);
        }

        // Fall back to venue name via Nominatim
        if (venueName != null && !venueName.isBlank()) {
            var venueCoords = geocodingService.geocode(venueName);
            if (venueCoords.isPresent()) {
                return new LocationInfo(venueCoords.get(), displayAddress);
            }
        }

        return new LocationInfo(Coordinates.tokyo(), displayAddress);
    }

    private String extractAddressText(JsonNode location) {
        JsonNode address = location.get("address");
        if (address == null) {
            return null;
        }

        String addressText = getJsonText(address, "name");
        if (addressText == null) {
            addressText = getJsonText(address, "streetAddress");
        }
        if (addressText == null) {
            addressText = getJsonText(address, "addressLocality");
        }
        return addressText;
    }

    private String buildDisplayAddress(String venueName, String addressText) {
        if (venueName != null && !venueName.isBlank() && addressText != null && !addressText.isBlank()) {
            return venueName + " / " + addressText;
        }
        if (venueName != null && !venueName.isBlank()) {
            return venueName;
        }
        return addressText;
    }

    /**
     * Maps Japanese addresses to coordinates.
     * First tries local mapping, then falls back to Nominatim geocoding.
     */
    private Coordinates geocodeAddress(String address) {
        if (address == null || address.isBlank()) {
            return Coordinates.tokyo();
        }

        if (address.contains(ONLINE_INDICATOR)) {
            return Coordinates.tokyo();
        }

        // Try local mapping first (fast, no API call)
        for (Map.Entry<String, Coordinates> entry : JAPAN_AREA_COORDINATES.entrySet()) {
            if (address.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Fall back to Nominatim geocoding for unknown addresses
        return geocodingService.geocode(address)
                .orElse(Coordinates.tokyo());
    }

    private String normalizeDescription(String text) {
        if (text == null) {
            return "";
        }

        String normalized = text
                .replaceAll("\\s+", " ")
                .trim();

        return normalized.length() > 300 ? normalized.substring(0, 300) + "..." : normalized;
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
