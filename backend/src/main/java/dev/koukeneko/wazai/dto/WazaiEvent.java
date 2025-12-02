package dev.koukeneko.wazai.dto;

import java.time.LocalDateTime;

/**
 * Represents a time-based event that appears on the Wazai map.
 * Events are characterized by having a specific start time and optional end time.
 *
 * Examples: tech meetups, conferences, workshops, community gatherings
 */
public record WazaiEvent(
        String id,
        String title,
        String description,
        String url,
        Coordinates coordinates,
        String address,
        LocalDateTime startTime,
        LocalDateTime endTime,
        EventType eventType,
        WazaiMapItem.DataSource source,
        WazaiMapItem.Country country
) implements WazaiMapItem {

    /**
     * Constructor with optional end time.
     * If end time is not provided, it defaults to null.
     */
    public WazaiEvent(
            String id,
            String title,
            String description,
            String url,
            Coordinates coordinates,
            String address,
            LocalDateTime startTime,
            EventType eventType,
            WazaiMapItem.DataSource source,
            WazaiMapItem.Country country
    ) {
        this(id, title, description, url, coordinates, address, startTime, null, eventType, source, country);
    }

    /**
     * Event type enumeration for categorizing different kinds of events.
     */
    public enum EventType {
        TECH_MEETUP,            // Technology meetups and user groups
        CONFERENCE,             // Large-scale conferences
        TECH_CONFERENCE,        // Tech-focused conferences (AWS Summit, Google I/O, etc.)
        WORKSHOP,               // Hands-on workshops
        COMMUNITY_GATHERING,    // General community events
        STUDY_GROUP,            // Reading groups and study sessions
        HACKATHON               // Hackathons and coding competitions
    }
}
