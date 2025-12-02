package dev.koukeneko.wazai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.koukeneko.wazai.dto.Coordinates;
import dev.koukeneko.wazai.service.GeocodingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Geocoding service using PositionStack API.
 * Better support for Japanese addresses than Nominatim.
 *
 * @see <a href="https://positionstack.com/documentation">PositionStack API</a>
 */
@Service
@ConditionalOnProperty(name = "positionstack.api.key", matchIfMissing = false)
public class PositionStackGeocodingService implements GeocodingService {

    private static final String API_URL = "http://api.positionstack.com/v1/forward";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Map<String, Optional<Coordinates>> geocodeCache;
    private final String apiKey;

    public PositionStackGeocodingService(
            WebClient.Builder webClientBuilder,
            @Value("${positionstack.api.key}") String apiKey) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = new ObjectMapper();
        this.geocodeCache = new ConcurrentHashMap<>();
        this.apiKey = apiKey;
        System.out.println("[PositionStack] Geocoding service initialized");
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
                // Remove postal code
                .replaceAll("〒\\d{3}-?\\d{4}\\s*", "")
                // Remove building floor info like "6F", "B1F", "18階"
                .replaceAll("\\s*B?\\d+F\\s*", " ")
                .replaceAll("\\s*\\d+階.*$", "")
                // Remove building names
                .replaceAll("\\s+[A-Za-z][A-Za-z0-9]*[^\\d\\s].*$", "")
                .replaceAll("\\s+.*ビル.*$", "")
                .replaceAll("\\s+.*タワー.*$", "")
                .replaceAll("\\s+.*センター.*$", "")
                .replaceAll("\\s+.*会館.*$", "")
                .replaceAll("\\s+.*ホール.*$", "")
                // Clean up extra spaces
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Optional<Coordinates> fetchCoordinates(String address) {
        try {
            System.out.println("[PositionStack] Geocoding: " + address);

            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = API_URL + "?access_key=" + apiKey + "&query=" + encodedAddress + "&country=JP&limit=1";

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
            System.err.println("[PositionStack] Error geocoding address '" + address + "': " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Coordinates> parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.get("data");

            if (data == null || !data.isArray() || data.isEmpty()) {
                System.out.println("[PositionStack] No results found");
                return Optional.empty();
            }

            JsonNode firstResult = data.get(0);
            double latitude = firstResult.get("latitude").asDouble();
            double longitude = firstResult.get("longitude").asDouble();

            System.out.println("[PositionStack] Found: " + latitude + ", " + longitude);
            return Optional.of(new Coordinates(latitude, longitude));

        } catch (Exception e) {
            System.err.println("[PositionStack] Error parsing response: " + e.getMessage());
            return Optional.empty();
        }
    }
}
