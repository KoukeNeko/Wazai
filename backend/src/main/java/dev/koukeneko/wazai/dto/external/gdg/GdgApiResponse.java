package dev.koukeneko.wazai.dto.external.gdg;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Root response from GDG Community API /api/search/ endpoint.
 * API Endpoint: https://gdg.community.dev/api/search/
 */
public record GdgApiResponse(
        GdgLocation location,

        @JsonProperty("results")
        List<GdgEvent> results
) {
}
