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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    private static final String API_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    // Japan geographic bounds for coordinate validation
    private static final double JAPAN_MIN_LAT = 24.0;
    private static final double JAPAN_MAX_LAT = 46.0;
    private static final double JAPAN_MIN_LNG = 122.0;
    private static final double JAPAN_MAX_LNG = 154.0;

    // Pattern to extract Japanese standard address format (都道府県 + 市区町村 + 町名 + 番地)
    private static final Pattern JAPANESE_ADDRESS_PATTERN = Pattern.compile(
            "(東京都|北海道|(?:京都|大阪)府|.{2,3}県)" +  // Prefecture
            "(.+?[市区町村])" +                           // City/Ward/Town/Village
            "(.+?(?:\\d+丁目\\d*番?\\d*号?|\\d+-\\d+(?:-\\d+)?|\\d+番地?\\d*号?))" // Full street address
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

        String normalizedAddress = normalizeAddress(address);
        return geocodeCache.computeIfAbsent(normalizedAddress, this::fetchCoordinates);
    }

    private String normalizeAddress(String address) {
        // First, try to extract the standard Japanese address format
        String extractedAddress = extractJapaneseAddress(address);
        if (extractedAddress != null) {
            System.out.println("[GoogleMaps] Extracted address: " + extractedAddress);
            return extractedAddress;
        }

        // Fallback: simple cleanup
        return address
                .replaceAll("〒\\d{3}-?\\d{4}\\s*", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String extractJapaneseAddress(String address) {
        // Remove postal code first
        String cleaned = address.replaceAll("〒\\d{3}-?\\d{4}\\s*", "");

        Matcher matcher = JAPANESE_ADDRESS_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            String prefecture = matcher.group(1);
            String city = matcher.group(2);
            String street = matcher.group(3);
            return prefecture + city + street;
        }
        return null;
    }

    private Optional<Coordinates> fetchCoordinates(String address) {
        try {
            System.out.println("[GoogleMaps] Geocoding: " + address);

            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            // Use components=country:JP to restrict results to Japan only
            String url = API_URL + "?address=" + encodedAddress
                    + "&key=" + apiKey
                    + "&language=ja"
                    + "&region=jp"
                    + "&components=country:JP";

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(REQUEST_TIMEOUT)
                    .block();

            if (response == null || response.isBlank()) {
                return Optional.empty();
            }

            return parseResponse(response);

        } catch (Exception e) {
            System.err.println("[GoogleMaps] Error geocoding address '" + address + "': " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Coordinates> parseResponse(String response) {
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

            JsonNode location = results.get(0).get("geometry").get("location");
            double latitude = location.get("lat").asDouble();
            double longitude = location.get("lng").asDouble();

            // Validate coordinates are within Japan bounds
            if (!isWithinJapanBounds(latitude, longitude)) {
                System.out.println("[GoogleMaps] Coordinates outside Japan bounds: " + latitude + ", " + longitude);
                return Optional.empty();
            }

            System.out.println("[GoogleMaps] Found: " + latitude + ", " + longitude);
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
}
