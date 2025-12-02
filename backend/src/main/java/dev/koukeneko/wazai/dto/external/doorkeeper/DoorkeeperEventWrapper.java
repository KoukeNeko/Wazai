package dev.koukeneko.wazai.dto.external.doorkeeper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Wrapper for Doorkeeper API event response.
 * The API returns events wrapped in an "event" object.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DoorkeeperEventWrapper(DoorkeeperEvent event) {}
