package dev.koukeneko.wazai.dto.external.gdg;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * External DTO for GDG Community chapter data.
 * Represents a local GDG chapter with its geographic information.
 */
public record GdgChapter(
        @JsonProperty("chapter_id")
        String chapterId,

        @JsonProperty("chapter_name")
        String chapterName,

        String country,

        String city,

        Double latitude,

        Double longitude,

        @JsonProperty("chapter_url")
        String chapterUrl
) {
}
