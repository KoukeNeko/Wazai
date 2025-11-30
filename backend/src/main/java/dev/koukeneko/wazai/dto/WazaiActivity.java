package dev.koukeneko.wazai.dto;

import java.time.LocalDateTime;

/**
 * Wazai platform's universal activity format.
 * This record represents any type of activity (tech events, clinics, community gatherings)
 * in a standardized format for frontend consumption.
 */
public record WazaiActivity(
        String id,              // Unique identifier (e.g., "connpass-123", "clinic-999")
        String title,           // Activity title (e.g., "Java Reading Club", "Internal Medicine Clinic")
        String description,     // Brief description
        String url,             // Link to the activity or location
        double latitude,        // Latitude for map display
        double longitude,       // Longitude for map display
        LocalDateTime startTime,// Start time (can be null for static locations like clinics)
        ActivityType type,      // Activity category
        String source           // Data source (e.g., "Connpass", "Google", "User")
) {
    /**
     * Activity type enumeration for categorizing different kinds of activities.
     */
    public enum ActivityType {
        TECH_EVENT,             // Technology-related events
        CLINIC,                 // Medical clinics
        COMMUNITY,              // Community gatherings
        OTHER                   // Other types
    }
}
