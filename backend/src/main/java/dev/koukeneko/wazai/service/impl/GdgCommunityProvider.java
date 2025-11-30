package dev.koukeneko.wazai.service.impl;

import dev.koukeneko.wazai.dto.Coordinates;
import dev.koukeneko.wazai.dto.WazaiEvent;
import dev.koukeneko.wazai.dto.WazaiMapItem;
import dev.koukeneko.wazai.service.ActivityProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.koukeneko.wazai.dto.WazaiEvent.EventType;
import static dev.koukeneko.wazai.dto.WazaiMapItem.Country;
import static dev.koukeneko.wazai.dto.WazaiMapItem.DataSource;

/**
 * Provider for GDG (Google Developer Groups) Community events.
 *
 * GDG Community (https://gdg.community.dev) hosts developer events worldwide,
 * including DevFests, study jams, workshops, and community meetups.
 *
 * Current implementation uses curated mock data for major Taiwan GDG chapters.
 * Future enhancement: Integrate with Algolia search API or official GDG API when available.
 */
@Service
public class GdgCommunityProvider implements ActivityProvider {

    private static final String PROVIDER_NAME = "GDG Community";
    private static final String BASE_URL = "https://gdg.community.dev";

    @Override
    public List<WazaiMapItem> search(String keyword) {
        if (isEmptyKeyword(keyword)) {
            return Collections.emptyList();
        }

        return searchGdgEvents(keyword);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    private boolean isEmptyKeyword(String keyword) {
        return keyword == null || keyword.isBlank();
    }

    private List<WazaiMapItem> searchGdgEvents(String keyword) {
        String normalizedKeyword = keyword.toLowerCase().trim();
        boolean showAll = normalizedKeyword.equals("all");

        List<WazaiMapItem> events = new ArrayList<>();

        // GDG Taipei
        if (showAll || containsKeyword(normalizedKeyword,
                "gdg", "google", "taipei", "台北", "devfest", "android", "cloud", "web")) {
            events.add(createGdgEvent(
                    "gdg-taipei-devfest-2026",
                    "GDG Taipei DevFest 2026",
                    "Google 技術年度盛會，涵蓋 Android、Cloud、Web、AI 等主題",
                    BASE_URL + "/gdg-taipei/",
                    Coordinates.taipei(),
                    LocalDateTime.of(2026, 11, 14, 9, 0),
                    LocalDateTime.of(2026, 11, 14, 18, 0),
                    Country.TAIWAN
            ));

            events.add(createGdgEvent(
                    "gdg-taipei-android-study-jam-2026",
                    "Android Study Jam - Taipei",
                    "深入學習 Jetpack Compose 和 Kotlin 最佳實踐",
                    BASE_URL + "/gdg-taipei/",
                    Coordinates.taipei(),
                    LocalDateTime.of(2026, 3, 15, 14, 0),
                    LocalDateTime.of(2026, 3, 15, 17, 0),
                    Country.TAIWAN
            ));
        }

        // GDG Kaohsiung
        if (showAll || containsKeyword(normalizedKeyword,
                "gdg", "google", "kaohsiung", "高雄", "devfest", "flutter", "firebase")) {
            events.add(createGdgEvent(
                    "gdg-kaohsiung-devfest-2026",
                    "GDG Kaohsiung DevFest 2026",
                    "南台灣最大 Google 技術研討會，聚焦 Flutter 與 Firebase 應用",
                    BASE_URL + "/gdg-kaohsiung/",
                    Coordinates.kaohsiung(),
                    LocalDateTime.of(2026, 10, 24, 9, 0),
                    LocalDateTime.of(2026, 10, 24, 18, 0),
                    Country.TAIWAN
            ));

            events.add(createGdgEvent(
                    "gdg-kaohsiung-firebase-workshop-2026",
                    "Firebase Workshop - Kaohsiung",
                    "實戰 Firebase Authentication 與 Firestore 資料庫",
                    BASE_URL + "/gdg-kaohsiung/",
                    Coordinates.kaohsiung(),
                    LocalDateTime.of(2026, 5, 9, 13, 30),
                    LocalDateTime.of(2026, 5, 9, 16, 30),
                    Country.TAIWAN
            ));
        }

        // GDG Taichung
        if (showAll || containsKeyword(normalizedKeyword,
                "gdg", "google", "taichung", "台中", "devfest", "cloud", "kubernetes")) {
            events.add(createGdgEvent(
                    "gdg-taichung-devfest-2026",
                    "GDG Taichung DevFest 2026",
                    "中部地區 Google 開發者節，探討 Cloud Native 與 Kubernetes",
                    BASE_URL + "/gdg-taichung/",
                    Coordinates.taichung(),
                    LocalDateTime.of(2026, 11, 7, 9, 0),
                    LocalDateTime.of(2026, 11, 7, 18, 0),
                    Country.TAIWAN
            ));

            events.add(createGdgEvent(
                    "gdg-taichung-cloud-workshop-2026",
                    "Google Cloud Workshop - Taichung",
                    "從零開始學習 Google Cloud Platform (GCP)",
                    BASE_URL + "/gdg-taichung/",
                    Coordinates.taichung(),
                    LocalDateTime.of(2026, 6, 20, 14, 0),
                    LocalDateTime.of(2026, 6, 20, 17, 0),
                    Country.TAIWAN
            ));
        }

        // GDG Hsinchu
        if (showAll || containsKeyword(normalizedKeyword,
                "gdg", "google", "hsinchu", "新竹", "machine learning", "ai", "tensorflow")) {
            events.add(createGdgEvent(
                    "gdg-hsinchu-ml-workshop-2026",
                    "Machine Learning Workshop - Hsinchu",
                    "TensorFlow 與 ML Kit 實戰應用",
                    BASE_URL + "/gdg-hsinchu/",
                    new Coordinates(24.8138, 120.9675), // Hsinchu
                    LocalDateTime.of(2026, 4, 18, 13, 0),
                    LocalDateTime.of(2026, 4, 18, 17, 0),
                    Country.TAIWAN
            ));
        }

        // GDG Tokyo (for keyword: japan, tokyo)
        if (showAll || containsKeyword(normalizedKeyword,
                "gdg", "google", "japan", "tokyo", "東京", "devfest")) {
            events.add(createGdgEvent(
                    "gdg-tokyo-devfest-2026",
                    "GDG Tokyo DevFest 2026",
                    "Japan's largest Google Developer community event",
                    BASE_URL + "/gdg-tokyo/",
                    Coordinates.tokyo(),
                    LocalDateTime.of(2026, 10, 31, 10, 0),
                    LocalDateTime.of(2026, 10, 31, 18, 0),
                    Country.JAPAN
            ));
        }

        // International DevFests
        if (containsKeyword(normalizedKeyword, "international", "asia", "devfest")) {
            events.add(createGdgEvent(
                    "gdg-singapore-devfest-2026",
                    "GDG Singapore DevFest 2026",
                    "Southeast Asia's premier Google Developer event",
                    BASE_URL + "/gdg-singapore/",
                    new Coordinates(1.3521, 103.8198), // Singapore
                    LocalDateTime.of(2026, 11, 21, 9, 0),
                    LocalDateTime.of(2026, 11, 21, 18, 0),
                    Country.TAIWAN // Using TAIWAN as placeholder for non-JP/TW countries
            ));
        }

        return events;
    }

    private boolean containsKeyword(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private WazaiEvent createGdgEvent(
            String id,
            String title,
            String description,
            String url,
            Coordinates coordinates,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Country country
    ) {
        return new WazaiEvent(
                id,
                title,
                description,
                url,
                coordinates,
                startTime,
                endTime,
                EventType.COMMUNITY_GATHERING,
                DataSource.GOOGLE_COMMUNITY,
                country
        );
    }
}
