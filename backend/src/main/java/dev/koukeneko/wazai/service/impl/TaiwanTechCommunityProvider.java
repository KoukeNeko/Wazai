package dev.koukeneko.wazai.service.impl;

import dev.koukeneko.wazai.dto.Coordinates;
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
 * Provides mock data for major Taiwan tech conferences and community events including:
 * PyCon TW, MOPCON, SITCON, HITCON, COSCUP, TOOCON, and more.
 *
 * TODO: Replace with actual API integration when available.
 */
@Service
public class TaiwanTechCommunityProvider implements ActivityProvider {

    private static final String PROVIDER_NAME = "Taiwan Tech Community";

    @Override
    public List<WazaiMapItem> search(String keyword) {
        return searchTaiwanTechEvents(keyword);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    /**
     * Search Taiwan tech events by keyword.
     * Returns mock data representing major Taiwan tech conferences.
     */
    private List<WazaiMapItem> searchTaiwanTechEvents(String keyword) {
        List<WazaiMapItem> events = new ArrayList<>();

        // If no keyword or generic search, return all events
        boolean showAll = keyword == null || keyword.isBlank() ||
                         containsKeyword(keyword, "台灣", "taiwan", "conference", "活動", "tech");

        // PyCon Taiwan - Python Conference
        if (showAll || containsKeyword(keyword, "python", "pycon", "AI", "機器學習", "data")) {
            events.add(createConference(
                    "pycon-tw-2026",
                    "PyCon Taiwan 2026",
                    "台灣最大的 Python 年會,涵蓋 AI、機器學習、資料科學等主題",
                    "https://tw.pycon.org",
                    Coordinates.taipei(),
                    LocalDateTime.of(2026, 9, 4, 9, 0),
                    LocalDateTime.of(2026, 9, 6, 18, 0)
            ));
        }

        // MOPCON - Mobile Conference
        if (showAll || containsKeyword(keyword, "mobile", "mopcon", "app", "ios", "android", "行動")) {
            events.add(createConference(
                    "mopcon-2026",
                    "MOPCON 2026",
                    "南部最大行動應用開發者年會,聚焦 iOS、Android、跨平台開發",
                    "https://mopcon.org",
                    Coordinates.kaohsiung(),
                    LocalDateTime.of(2026, 10, 24, 9, 0),
                    LocalDateTime.of(2026, 10, 25, 18, 0)
            ));
        }

        // SITCON - Students in Taiwan Conference
        if (showAll || containsKeyword(keyword, "student", "sitcon", "學生", "教育")) {
            events.add(createConference(
                    "sitcon-2026",
                    "SITCON 2026 學生計算機年會",
                    "由學生自發舉辦的資訊技術研討會,為學生提供交流平台",
                    "https://sitcon.org",
                    Coordinates.taipei(),
                    LocalDateTime.of(2026, 8, 8, 9, 0),
                    LocalDateTime.of(2026, 8, 8, 18, 0)
            ));
        }

        // HITCON - Hacks in Taiwan Conference
        if (showAll || containsKeyword(keyword, "security", "hitcon", "hacking", "資安", "駭客")) {
            events.add(createConference(
                    "hitcon-2026",
                    "HITCON 2026",
                    "台灣駭客年會,亞洲最大資訊安全研討會",
                    "https://hitcon.org",
                    Coordinates.taipei(),
                    LocalDateTime.of(2026, 8, 21, 9, 0),
                    LocalDateTime.of(2026, 8, 22, 18, 0)
            ));
        }

        // COSCUP - Conference for Open Source Coders, Users & Promoters
        if (showAll || containsKeyword(keyword, "open source", "coscup", "開源", "linux")) {
            events.add(createConference(
                    "coscup-2026",
                    "COSCUP 2026",
                    "開源人年會,推廣開源軟體文化的大型研討會",
                    "https://coscup.org",
                    Coordinates.taipei(),
                    LocalDateTime.of(2026, 8, 1, 9, 0),
                    LocalDateTime.of(2026, 8, 2, 18, 0)
            ));
        }

        // ModernWeb - Modern Web Conference
        if (showAll || containsKeyword(keyword, "web", "javascript", "frontend", "react", "vue", "angular", "前端")) {
            events.add(createConference(
                    "modernweb-2026",
                    "ModernWeb 2026",
                    "現代網頁技術研討會,專注於前後端最新技術趨勢",
                    "https://modernweb.tw",
                    Coordinates.taipei(),
                    LocalDateTime.of(2026, 7, 18, 9, 0),
                    LocalDateTime.of(2026, 7, 19, 18, 0)
            ));
        }

        // DevOpsDays Taipei
        if (showAll || containsKeyword(keyword, "devops", "cloud", "kubernetes", "docker", "運維")) {
            events.add(createConference(
                    "devopsdays-taipei-2026",
                    "DevOpsDays Taipei 2026",
                    "DevOps 實踐者交流大會,涵蓋 CI/CD、容器化、雲端架構",
                    "https://devopsdays.tw",
                    Coordinates.taipei(),
                    LocalDateTime.of(2026, 9, 12, 9, 0),
                    LocalDateTime.of(2026, 9, 13, 18, 0)
            ));
        }

        // JSDC - JavaScript Developer Conference
        if (showAll || containsKeyword(keyword, "javascript", "jsdc", "js", "node", "typescript")) {
            events.add(createConference(
                    "jsdc-2026",
                    "JSDC 2026",
                    "JavaScript 開發者年會,全台最大 JS 技術盛會",
                    "https://jsdc.tw",
                    Coordinates.taipei(),
                    LocalDateTime.of(2026, 10, 17, 9, 0),
                    LocalDateTime.of(2026, 10, 18, 18, 0)
            ));
        }

        // GDG DevFest Taipei
        if (showAll || containsKeyword(keyword, "google", "gdg", "devfest", "android", "flutter")) {
            events.add(createEvent(
                    "gdg-devfest-taipei-2026",
                    "GDG DevFest Taipei 2026",
                    "Google 開發者社群年度盛會,探討 Google 最新技術",
                    "https://gdg.taipei",
                    Coordinates.taipei(),
                    LocalDateTime.of(2026, 11, 14, 9, 0),
                    EventType.COMMUNITY_GATHERING
            ));
        }

        // Agile Summit
        if (showAll || containsKeyword(keyword, "agile", "scrum", "敏捷", "專案管理")) {
            events.add(createConference(
                    "agile-summit-2026",
                    "Agile Summit 2026",
                    "敏捷開發年會,分享敏捷實踐經驗與團隊協作方法",
                    "https://agilesummit.tw",
                    Coordinates.taichung(),
                    LocalDateTime.of(2026, 6, 27, 9, 0),
                    LocalDateTime.of(2026, 6, 28, 18, 0)
            ));
        }

        return events;
    }

    private WazaiEvent createConference(
            String id,
            String title,
            String description,
            String url,
            Coordinates coordinates,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        return new WazaiEvent(
                id,
                title,
                description,
                url,
                coordinates,
                startTime,
                endTime,
                EventType.CONFERENCE,
                DataSource.TAIWAN_TECH_COMMUNITY,
                Country.TAIWAN
        );
    }

    private WazaiEvent createEvent(
            String id,
            String title,
            String description,
            String url,
            Coordinates coordinates,
            LocalDateTime startTime,
            EventType eventType
    ) {
        return new WazaiEvent(
                id,
                title,
                description,
                url,
                coordinates,
                startTime,
                eventType,
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
