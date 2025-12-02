package dev.koukeneko.wazai.dto.external.gdg;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Detailed GDG chapter information from chapter_region API.
 * Includes geographic coordinates provided by the API.
 */
public record GdgChapterInfo(
        Boolean active,
        String city,
        String country,
        Long id,
        @JsonProperty("hide_country_info")
        Boolean hideCountryInfo,
        Double latitude,
        Double longitude,
        Object logo,
        Object picture,
        String state,
        String title,
        @JsonProperty("relative_url")
        String relativeUrl,
        String url
) {
}
