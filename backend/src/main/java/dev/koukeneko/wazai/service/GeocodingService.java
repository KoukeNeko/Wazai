package dev.koukeneko.wazai.service;

import dev.koukeneko.wazai.dto.Coordinates;

import java.util.Optional;

/**
 * Service for converting addresses to geographic coordinates.
 */
public interface GeocodingService {

    /**
     * Converts an address string to coordinates.
     *
     * @param address the address to geocode
     * @return Optional containing coordinates if found, empty otherwise
     */
    Optional<Coordinates> geocode(String address);
}
