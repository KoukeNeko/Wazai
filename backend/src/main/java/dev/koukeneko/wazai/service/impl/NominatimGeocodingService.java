package dev.koukeneko.wazai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.koukeneko.wazai.dto.Coordinates;
import dev.koukeneko.wazai.service.GeocodingService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Geocoding service using OpenStreetMap Nominatim API.
 *
 * Nominatim is free but requires:
 * - Max 1 request per second
 * - User-Agent header identifying the application
 * - Caching results to minimize API calls
 *
 * @see <a href="https://nominatim.org/release-docs/develop/api/Search/">Nominatim API</a>
 */
@Service
public class NominatimGeocodingService implements GeocodingService {

    private static final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "WazaiMaps/1.0 (https://github.com/koukeneko/wazai)";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final long RATE_LIMIT_MS = 1100;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Map<String, Optional<Coordinates>> geocodeCache;

    private long lastRequestTime = 0;

    public NominatimGeocodingService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .defaultHeader("User-Agent", USER_AGENT)
                .build();
        this.objectMapper = new ObjectMapper();
        this.geocodeCache = new ConcurrentHashMap<>();
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
                .replaceAll("〒\\d{3}-?\\d{4}\\s*", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Optional<Coordinates> fetchCoordinates(String address) {
        // Try original address first
        var result = tryGeocode(address);
        if (result.isPresent()) {
            return result;
        }

        // Try with "東京" suffix if not found
        result = tryGeocode(address + " 東京");
        if (result.isPresent()) {
            return result;
        }

        // Try with "日本" suffix
        return tryGeocode(address + " 日本");
    }

    private Optional<Coordinates> tryGeocode(String address) {
        try {
            System.out.println("[Nominatim] Geocoding: " + address);
            enforceRateLimit();

            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = NOMINATIM_API_URL + "?q=" + encodedAddress + "&format=json&limit=1&countrycodes=jp";

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
            System.err.println("[Nominatim] Error geocoding address '" + address + "': " + e.getMessage());
            return Optional.empty();
        }
    }

    private synchronized void enforceRateLimit() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;

        if (timeSinceLastRequest < RATE_LIMIT_MS) {
            try {
                Thread.sleep(RATE_LIMIT_MS - timeSinceLastRequest);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        lastRequestTime = System.currentTimeMillis();
    }

    private Optional<Coordinates> parseResponse(String response) {
        try {
            JsonNode results = objectMapper.readTree(response);

            if (!results.isArray() || results.isEmpty()) {
                System.out.println("[Nominatim] No results found");
                return Optional.empty();
            }

            JsonNode firstResult = results.get(0);
            double latitude = firstResult.get("lat").asDouble();
            double longitude = firstResult.get("lon").asDouble();

            System.out.println("[Nominatim] Found: " + latitude + ", " + longitude);
            return Optional.of(new Coordinates(latitude, longitude));

        } catch (Exception e) {
            System.err.println("[Nominatim] Error parsing response: " + e.getMessage());
            return Optional.empty();
        }
    }
}
