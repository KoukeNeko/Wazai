package dev.koukeneko.wazai.dto.external.gdg;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Location information from GDG Community API.
 * This represents the user's current location or search center.
 */
public record GdgLocation(
        String city,

        String country,

        @JsonProperty("country_code")
        String countryCode,

        Double latitude,

        Double longitude
) {
}
