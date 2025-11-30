package dev.koukeneko.wazai.service.impl;

import dev.koukeneko.wazai.dto.WazaiEvent;
import dev.koukeneko.wazai.dto.WazaiEvent.EventType;
import dev.koukeneko.wazai.dto.WazaiMapItem;
import dev.koukeneko.wazai.dto.WazaiMapItem.DataSource;
import dev.koukeneko.wazai.dto.WazaiMapItem.Country;
import dev.koukeneko.wazai.service.ActivityProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Taiwan Tech Community provider implementation.
 * This is a demonstration provider showing how easy it is to add new data sources.
 * Currently returns mock data - replace with actual API integration when available.
 */
@Service
public class TaiwanTechCommunityProvider implements ActivityProvider {

    private static final String PROVIDER_NAME = "Taiwan Tech Community";

    @Override
    public List<WazaiMapItem> search(String keyword) {
        // TODO: Replace with actual API call to Taiwan tech community platform
        return createMockTaiwanEvents(keyword);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    /**
     * Mock data generator for demonstration purposes.
     * Replace this with actual API integration.
     */
    private List<WazaiMapItem> createMockTaiwanEvents(String keyword) {
        List<WazaiMapItem> events = new ArrayList<>();

        if (containsKeyword(keyword, "java", "程式", "coding")) {
            events.add(createEvent(
                    "tw-1",
                    "台北 Java 開發者聚會",
                    "每月定期聚會,討論 Spring Boot 與微服務架構",
                    "https://example.com/tw-java-meetup",
                    25.0330,  // Taipei latitude
                    121.5654, // Taipei longitude
                    LocalDateTime.of(2025, 12, 15, 14, 0)
            ));
        }

        if (containsKeyword(keyword, "python", "AI", "機器學習")) {
            events.add(createEvent(
                    "tw-2",
                    "Python Taiwan 年會籌備會",
                    "討論 PyCon TW 2026 籌備事項",
                    "https://pycon.tw",
                    25.0478,
                    121.5318,
                    LocalDateTime.of(2025, 12, 20, 19, 0)
            ));
        }

        return events;
    }

    private WazaiEvent createEvent(
            String id,
            String title,
            String description,
            String url,
            double latitude,
            double longitude,
            LocalDateTime startTime
    ) {
        return new WazaiEvent(
                id,
                title,
                description,
                url,
                latitude,
                longitude,
                startTime,
                EventType.COMMUNITY_GATHERING,
                DataSource.TAIWAN_TECH_COMMUNITY,
                Country.TAIWAN
        );
    }

    private boolean containsKeyword(String searchTerm, String... keywords) {
        if (searchTerm == null) {
            return false;
        }

        String lowerSearchTerm = searchTerm.toLowerCase();
        for (String keyword : keywords) {
            if (lowerSearchTerm.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
