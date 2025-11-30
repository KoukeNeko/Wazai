package dev.koukeneko.wazai.dto;

/**
 * Represents geographic coordinates (latitude and longitude).
 * This is a Value Object that encapsulates location data as a single cohesive unit.
 *
 * Following DDD principles, coordinates are always used together and should not be split.
 */
public record Coordinates(
        double latitude,
        double longitude
) {
    /**
     * Validates that coordinates are within valid ranges.
     */
    public Coordinates {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90, got: " + latitude
            );
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180, got: " + longitude
            );
        }
    }

    /**
     * Factory method for common Taiwan locations.
     */
    public static Coordinates taipei() {
        return new Coordinates(25.0330, 121.5654);
    }

    public static Coordinates kaohsiung() {
        return new Coordinates(22.6273, 120.3014);
    }

    public static Coordinates taichung() {
        return new Coordinates(24.1477, 120.6736);
    }

    /**
     * Factory method for Tokyo locations.
     */
    public static Coordinates tokyo() {
        return new Coordinates(35.6812, 139.7671);
    }

    /**
     * Calculate distance to another coordinate (in kilometers).
     * Uses Haversine formula.
     */
    public double distanceTo(Coordinates other) {
        final int EARTH_RADIUS_KM = 6371;

        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude))
                * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    @Override
    public String toString() {
        return String.format("(%.4f, %.4f)", latitude, longitude);
    }
}
