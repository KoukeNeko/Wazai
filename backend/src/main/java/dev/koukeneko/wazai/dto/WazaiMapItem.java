package dev.koukeneko.wazai.dto;

/**
 * Sealed interface representing any item that can be displayed on the Wazai map.
 * This interface uses Java's sealed types to ensure type safety and exhaustive pattern matching.
 *
 * All map items share common properties (id, location, source), but have different
 * characteristics based on whether they are events (time-based) or places (static).
 */
public sealed interface WazaiMapItem permits WazaiEvent, WazaiPlace {

    String id();
    String title();
    String description();
    String url();
    Coordinates coordinates();
    String address();
    DataSource source();
    Country country();

    /**
     * Data source enumeration.
     */
    public enum DataSource {
        CONNPASS,
        TAIWAN_TECH_COMMUNITY,
        AWS_EVENTS,
        GOOGLE_COMMUNITY,
        MEETUP,
        TECHPLAY,
        DOORKEEPER
    }

    /**
     * Country/region enumeration for geographic classification.
     */
    enum Country {
        JAPAN,                  // Japan
        TAIWAN,                  // Taiwan
        DEFAULT
    }
}
