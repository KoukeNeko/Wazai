package dev.koukeneko.wazai.dto;

/**
 * Represents a static location/place that appears on the Wazai map.
 * Places are characterized by being permanently located at a specific address,
 * rather than being time-based events.
 * <p>
 * Examples: clinics, hospitals, cafes, coworking spaces, restaurants
 */
public record WazaiPlace(
        String id,
        String title,
        String description,
        String url,
        Coordinates coordinates,
        String address,
        BusinessHours businessHours,
        PlaceType placeType,
        WazaiMapItem.DataSource source,
        WazaiMapItem.Country country
) implements WazaiMapItem {

    /**
     * Constructor with optional business hours.
     */
    public WazaiPlace(
            String id,
            String title,
            String description,
            String url,
            Coordinates coordinates,
            String address,
            PlaceType placeType,
            WazaiMapItem.DataSource source,
            WazaiMapItem.Country country
    ) {
        this(id, title, description, url, coordinates, address, null, placeType, source, country);
    }

    /**
     * Place type enumeration for categorizing different kinds of locations.
     */
    public enum PlaceType {
        CLINIC,                 // Medical clinic
        HOSPITAL,               // Hospital
        CAFE,                   // Cafe or coffee shop
        COWORKING_SPACE,        // Coworking space
        RESTAURANT,             // Restaurant
        LIBRARY,                // Library
        COMMUNITY_CENTER,       // Community center
        OTHER                   // Other types
    }

    /**
     * Business hours information for places.
     * This is a flexible string field to accommodate various formats:
     * - "Mon-Fri: 09:00-18:00"
     * - "24/7"
     * - "Weekdays 9am-6pm, Sat 10am-2pm"
     * - "By appointment only"
     * <p>
     * For structured hour parsing, consider integrating with Google Places API
     * or implementing a dedicated BusinessHoursParser service.
     */
    public record BusinessHours(
            String displayText  // Human-readable business hours description
    ) {
        /**
         * Factory method for creating simple daily hours.
         */
        public static BusinessHours daily(String openTime, String closeTime) {
            return new BusinessHours(String.format("Daily: %s-%s", openTime, closeTime));
        }

        /**
         * Factory method for creating weekday hours.
         */
        public static BusinessHours weekdays(String openTime, String closeTime) {
            return new BusinessHours(String.format("Mon-Fri: %s-%s", openTime, closeTime));
        }

        /**
         * Factory method for 24/7 operations.
         */
        public static BusinessHours alwaysOpen() {
            return new BusinessHours("24/7");
        }
    }
}
