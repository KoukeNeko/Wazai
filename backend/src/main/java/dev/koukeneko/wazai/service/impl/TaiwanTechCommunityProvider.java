package dev.koukeneko.wazai.service.impl;

import dev.koukeneko.wazai.dto.Coordinates;
import dev.koukeneko.wazai.dto.WazaiEvent;
import dev.koukeneko.wazai.dto.WazaiEvent.EventType;
import dev.koukeneko.wazai.dto.WazaiMapItem;
import dev.koukeneko.wazai.dto.WazaiMapItem.Country;
import dev.koukeneko.wazai.dto.WazaiMapItem.DataSource;
import dev.koukeneko.wazai.service.ActivityProvider;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Taiwan Tech Community provider implementation.
 * Provides data for major Taiwan tech conferences and community events.
 * Data is loaded from 'taiwan-tech-events.yml'.
 */
@Service
public class TaiwanTechCommunityProvider implements ActivityProvider {

    private static final Logger logger = LoggerFactory.getLogger(TaiwanTechCommunityProvider.class);
    private static final String PROVIDER_NAME = "Taiwan Tech Community";
    private static final String YAML_FILE = "taiwan-tech-events.yml";
    
    private List<WazaiEvent> cachedEvents = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadEventsFromYaml();
    }

    private void loadEventsFromYaml() {
        try {
            ClassPathResource resource = new ClassPathResource(YAML_FILE);
            if (!resource.exists()) {
                logger.warn("YAML file '{}' not found. No events loaded.", YAML_FILE);
                return;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                Yaml yaml = new Yaml();
                Map<String, List<Map<String, Object>>> data = yaml.load(inputStream);
                
                if (data != null && data.containsKey("events")) {
                    List<Map<String, Object>> rawEvents = data.get("events");
                    this.cachedEvents = rawEvents.stream()
                            .map(this::mapToWazaiEvent)
                            .collect(Collectors.toList());
                    logger.info("Loaded {} events from {}", cachedEvents.size(), YAML_FILE);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load events from YAML", e);
        }
    }

    private WazaiEvent mapToWazaiEvent(Map<String, Object> raw) {
        String id = (String) raw.get("id");
        String title = (String) raw.get("title");
        String description = (String) raw.get("description");
        String url = (String) raw.get("url");
        
        Double lat = raw.get("latitude") instanceof Number ? ((Number) raw.get("latitude")).doubleValue() : 0.0;
        Double lon = raw.get("longitude") instanceof Number ? ((Number) raw.get("longitude")).doubleValue() : 0.0;
        Coordinates coordinates = new Coordinates(lat, lon);

        String startStr = (String) raw.get("start");
        String endStr = (String) raw.get("end");
        
        LocalDateTime start = startStr != null ? LocalDateTime.parse(startStr) : LocalDateTime.now();
        LocalDateTime end = endStr != null ? LocalDateTime.parse(endStr) : start.plusHours(2);

        String typeStr = (String) raw.get("type");
        EventType type = EventType.CONFERENCE; // Default
        if (typeStr != null) {
            try {
                type = EventType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                // Keep default
            }
        }

        return new WazaiEvent(
                id,
                title,
                description,
                url,
                coordinates,
                start,
                end,
                type,
                DataSource.TAIWAN_TECH_COMMUNITY,
                Country.TAIWAN
        );
    }

    @Override
    public List<WazaiMapItem> search(String keyword) {
        if (cachedEvents.isEmpty()) {
            // Try reloading if empty (e.g. if file was added later or initial load failed)
            // In a real prod scenario, we might want a more robust reloading strategy.
            loadEventsFromYaml();
        }
        
        return searchTaiwanTechEvents(keyword);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    /**
     * Search Taiwan tech events by keyword.
     */
    private List<WazaiMapItem> searchTaiwanTechEvents(String keyword) {
        // If no keyword or generic search, return all events
        boolean showAll = keyword == null || keyword.isBlank();

        if (showAll) {
            return new ArrayList<>(cachedEvents);
        }

        return cachedEvents.stream()
                .filter(event -> matchesKeyword(event, keyword))
                .collect(Collectors.toList());
    }

    private boolean matchesKeyword(WazaiEvent event, String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        
        // Check title
        if (event.title() != null && event.title().toLowerCase().contains(lowerKeyword)) {
            return true;
        }
        
        // Check description
        if (event.description() != null && event.description().toLowerCase().contains(lowerKeyword)) {
            return true;
        }
        
        // Check ID (sometimes contains useful info like 'pycon')
        if (event.id() != null && event.id().toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        return false;
    }
}
