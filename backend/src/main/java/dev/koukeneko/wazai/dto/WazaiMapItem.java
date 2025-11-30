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
    DataSource source();
    Country country();

    /**
     * Data source enumeration for identifying activity providers.
     * Using enum ensures type safety and prevents invalid source values.
     */
    enum DataSource {
        CONNPASS,               // Connpass tech events platform
        TAIWAN_TECH_COMMUNITY,  // Taiwan tech community events
        GOOGLE_COMMUNITY,       // Google community platform
        INTERNAL_DATABASE,      // Wazai's internal database
        USER_SUBMITTED          // User-submitted items
    }

    /**
     * Country/region enumeration for geographic classification.
     */
    enum Country {
        JAPAN,                  // Japan
        TAIWAN                  // Taiwan
    }
}
