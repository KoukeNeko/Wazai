package dev.koukeneko.wazai.dto.external.gdg;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Event data from GDG Community API.
 * Represents a single GDG event with its chapter and metadata.
 * API Reference: https://gdg.community.dev/api/search/
 */
public record GdgEvent(
        Long id,

        String title,

        @JsonProperty("description_short")
        String descriptionShort,

        GdgChapter chapter,

        String city,

        @JsonProperty("start_date")
        String startDate,

        String url,

        @JsonProperty("relative_url")
        String relativeUrl,

        @JsonProperty("video_url")
        String videoUrl,

        @JsonProperty("event_type_title")
        String eventTypeTitle,

        List<String> tags,

        @JsonProperty("allows_cohosting")
        Boolean allowsCohosting,

        @JsonProperty("result_type")
        String resultType
) {
}
