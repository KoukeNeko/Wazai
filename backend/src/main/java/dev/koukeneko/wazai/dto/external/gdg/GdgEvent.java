package dev.koukeneko.wazai.dto.external.gdg;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * External DTO for GDG Community event data.
 * This represents the raw event structure from gdg.community.dev.
 *
 * GDG Community uses Algolia search with dynamic loading,
 * so this structure may need adjustment based on actual API response.
 */
public record GdgEvent(
        @JsonProperty("event_id")
        String eventId,

        String title,

        String description,

        @JsonProperty("event_url")
        String eventUrl,

        @JsonProperty("start_date")
        String startDate,

        @JsonProperty("end_date")
        String endDate,

        @JsonProperty("chapter_name")
        String chapterName,

        @JsonProperty("chapter_location")
        String chapterLocation,

        Double latitude,

        Double longitude,

        @JsonProperty("event_type")
        String eventType
) {
}
