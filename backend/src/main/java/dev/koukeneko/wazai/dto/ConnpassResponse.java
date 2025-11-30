package dev.koukeneko.wazai.dto;

import java.util.ArrayList;
import java.util.List;

// WIth record, we can define immutable data carriers in a concise way, getters, constructors, equals, hashCode, and toString are generated automatically.
// Variable names should follow JSON property names for seamless serialization/deserialization.
public record ConnpassResponse(
        int results_returned,
        List<ConnpassEvent> events
) {}
