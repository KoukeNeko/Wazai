package dev.koukeneko.wazai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.koukeneko.wazai.dto.Coordinates;
import dev.koukeneko.wazai.service.GeocodingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Geocoding service using Google Maps Geocoding API.
 * Provides the most accurate geocoding for Japanese addresses.
 * This is the primary geocoding service when Google Maps API key is configured.
 *
 * @see <a href="https://developers.google.com/maps/documentation/geocoding">Google Maps Geocoding API</a>
 */
@Service
@Primary
@ConditionalOnProperty(name = "google.maps.api.key", matchIfMissing = false)
public class GoogleMapsGeocodingService implements GeocodingService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    // Japan geographic bounds for coordinate validation
    private static final double JAPAN_MIN_LAT = 24.0;
    private static final double JAPAN_MAX_LAT = 46.0;
    private static final double JAPAN_MIN_LNG = 122.0;
    private static final double JAPAN_MAX_LNG = 154.0;

    // Pattern to extract Japanese address starting from prefecture
    // Japanese address format: 都道府県 → 市区町村 → 町名 → 丁目/番地/号 → 建物名
    private static final Pattern PREFECTURE_PATTERN = Pattern.compile(
            "(東京都|北海道|(?:京都|大阪)府|.{2,3}県)(.+)"
    );

    // Pattern to extract city/ward for addresses without prefecture
    // Matches: 区, 市, 町, 村
    private static final Pattern CITY_WARD_PATTERN = Pattern.compile(
            "(.+?[区市町村])(.+)"
    );

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Map<String, Optional<Coordinates>> geocodeCache;
    private final String apiKey;

    public GoogleMapsGeocodingService(
            WebClient.Builder webClientBuilder,
            @Value("${google.maps.api.key}") String apiKey) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = new ObjectMapper();
        this.geocodeCache = new ConcurrentHashMap<>();
        this.apiKey = apiKey;
        System.out.println("[GoogleMaps] Geocoding service initialized");
    }

    @Override
    public Optional<Coordinates> geocode(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }

        // Just remove postal code, pass original address to Google Maps
        String normalizedAddress = address.replaceAll("〒?\\d{3}-?\\d{4}\\s*", "").trim();
        return geocodeCache.computeIfAbsent(normalizedAddress, addr -> fetchCoordinates(addr, address));
    }

    private Optional<Coordinates> fetchCoordinates(String address, String originalAddress) {
        try {
            System.out.println("[GoogleMaps] Geocoding: " + address);

            String response = webClient.get()
                    .uri("https://maps.googleapis.com/maps/api/geocode/json?address={address}&key={key}&language=ja&region=jp",
                            address, apiKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(REQUEST_TIMEOUT)
                    .block();

            if (response == null || response.isBlank()) {
                return Optional.empty();
            }

            return parseResponse(response, originalAddress);

        } catch (Exception e) {
            System.err.println("[GoogleMaps] Error geocoding address '" + address + "': " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Coordinates> parseResponse(String response, String originalAddress) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String status = root.get("status").asText();

            if (!"OK".equals(status)) {
                System.out.println("[GoogleMaps] API returned status: " + status);
                return Optional.empty();
            }

            JsonNode results = root.get("results");
            if (results == null || !results.isArray() || results.isEmpty()) {
                System.out.println("[GoogleMaps] No results found");
                return Optional.empty();
            }

            // Check formatted_address to verify result is actually in expected location
            JsonNode firstResult = results.get(0);
            String formattedAddress = firstResult.has("formatted_address")
                    ? firstResult.get("formatted_address").asText()
                    : "";

            JsonNode location = firstResult.get("geometry").get("location");
            double latitude = location.get("lat").asDouble();
            double longitude = location.get("lng").asDouble();

            // Validate coordinates are within Japan bounds
            if (!isWithinJapanBounds(latitude, longitude)) {
                System.out.println("[GoogleMaps] Coordinates outside Japan bounds: " + latitude + ", " + longitude);
                return Optional.empty();
            }

            // Verify the result matches expected location from original address
            if (!verifyLocationMatch(originalAddress, formattedAddress)) {
                System.out.println("[GoogleMaps] Location mismatch! Original: " + originalAddress + ", Got: " + formattedAddress);
                return Optional.empty();
            }

            System.out.println("[GoogleMaps] Found: " + latitude + ", " + longitude + " (" + formattedAddress + ")");
            return Optional.of(new Coordinates(latitude, longitude));

        } catch (Exception e) {
            System.err.println("[GoogleMaps] Error parsing response: " + e.getMessage());
            return Optional.empty();
        }
    }

    private boolean isWithinJapanBounds(double latitude, double longitude) {
        return latitude >= JAPAN_MIN_LAT && latitude <= JAPAN_MAX_LAT
                && longitude >= JAPAN_MIN_LNG && longitude <= JAPAN_MAX_LNG;
    }

    private boolean verifyLocationMatch(String originalAddress, String formattedAddress) {
        // Try to verify by prefecture first
        Matcher prefectureMatcher = PREFECTURE_PATTERN.matcher(originalAddress);
        if (prefectureMatcher.find()) {
            String expectedPrefecture = prefectureMatcher.group(1);
            if (!formattedAddress.contains(expectedPrefecture)) {
                return false;
            }
            return true;
        }

        // If no prefecture, try to verify by city/ward (区, 市, 町, 村)
        Matcher cityWardMatcher = CITY_WARD_PATTERN.matcher(originalAddress);
        if (cityWardMatcher.find()) {
            String expectedCityWard = cityWardMatcher.group(1);
            if (!formattedAddress.contains(expectedCityWard)) {
                System.out.println("[GoogleMaps] City/ward mismatch! Expected: " + expectedCityWard + ", Got: " + formattedAddress);
                return false;
            }
            return true;
        }

        // Can't verify, assume OK
        return true;
    }
}
