package dev.koukeneko.wazai.dto.external.meetup;

import java.util.Map;

public record MeetupGqlRequest(String query, Map<String, Object> variables) {}
