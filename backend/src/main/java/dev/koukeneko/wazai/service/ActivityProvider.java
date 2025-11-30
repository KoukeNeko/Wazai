package dev.koukeneko.wazai.service;

import dev.koukeneko.wazai.dto.WazaiActivity;

import java.util.List;

/**
 * Interface defining the contract for all activity data providers.
 * Any service that provides activity data must implement this interface,
 * ensuring consistent behavior across different data sources.
 */
public interface ActivityProvider {

    /**
     * Search for activities based on a keyword.
     *
     * @param keyword the search term
     * @return a list of activities matching the keyword in WazaiActivity format
     */
    List<WazaiActivity> search(String keyword);

    /**
     * Get the name of this provider.
     *
     * @return the provider's name (e.g., "Connpass", "Google", "InternalDB")
     */
    String getProviderName();
}
