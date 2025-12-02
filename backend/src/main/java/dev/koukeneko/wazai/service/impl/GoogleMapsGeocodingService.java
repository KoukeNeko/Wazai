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
        return address
                // Remove postal code patterns
                .replaceAll("〒\\d{3}-?\\d{4}\\s*", "")
                // Clean up extra spaces
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Optional<Coordinates> fetchCoordinates(String address) {
        try {
            System.out.println("[GoogleMaps] Geocoding: " + address);

            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = API_URL + "?address=" + encodedAddress + "&key=" + apiKey + "&language=ja&region=jp";

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

            System.out.println("[GoogleMaps] Found: " + latitude + ", " + longitude);
            return Optional.of(new Coordinates(latitude, longitude));

        } catch (Exception e) {
            System.err.println("[GoogleMaps] Error parsing response: " + e.getMessage());
            return Optional.empty();
        }
    }
}
