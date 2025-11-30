package dev.koukeneko.wazai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConnpassEvent(
        long event_id,
        String title,
        String event_url,
        String started_at,
        String address,
        String place,

        @JsonProperty("catch") // 'catch' is a reserved keyword in Java, so we use @JsonProperty to map it correctly
        String catchMessage
) {}
