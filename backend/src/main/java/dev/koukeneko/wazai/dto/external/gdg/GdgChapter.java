package dev.koukeneko.wazai.dto.external.gdg;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Chapter information from GDG Community API.
 * Represents a local GDG chapter with its geographic and organizational details.
 */
public record GdgChapter(
        Long id,

        @JsonProperty("chapter_location")
        String chapterLocation,

        String city,

        String country,

        @JsonProperty("country_name")
        String countryName,

        String description,

        @JsonProperty("hide_country_info")
        Boolean hideCountryInfo,

        String state,

        String timezone,

        String title,

        @JsonProperty("relative_url")
        String relativeUrl,

        String url
) {
}
