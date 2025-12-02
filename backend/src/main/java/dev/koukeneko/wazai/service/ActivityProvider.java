package dev.koukeneko.wazai.service;

import dev.koukeneko.wazai.dto.WazaiMapItem;

import java.util.List;

/**
 * Interface defining the contract for all map item data providers.
 * Any service that provides map data (events, places, etc.) must implement this interface,
 * ensuring consistent behavior across different data sources.
 *
 * Providers can return either WazaiEvent (time-based activities) or WazaiPlace (static locations),
 * both of which implement the WazaiMapItem sealed interface.
 */
public interface ActivityProvider {

    /**
     * Search for map items based on a keyword.
     *
     * @param keyword the search term
     * @return a list of map items matching the keyword (can be events or places)
     */
    List<WazaiMapItem> search(String keyword);

    /**
     * Get the name of this provider.
     *
     * @return the provider's name (e.g., "Connpass", "Google", "InternalDB")
     */
    String getProviderName();
}
