package dev.koukeneko.wazai.service;

import dev.koukeneko.wazai.dto.WazaiMapItem;
import org.springframework.stereotype.Service;

import java.util.List;

import static dev.koukeneko.wazai.dto.WazaiMapItem.Country;

/**
 * Aggregator service that coordinates multiple map item providers.
 * This service acts as a facade, delegating search requests to all registered
 * providers and combining their results into a unified response.
 *
 * Providers can return either events (WazaiEvent) or places (WazaiPlace),
 * all implementing the WazaiMapItem sealed interface.
 */
@Service
public class WazaiSearchService {

    private final List<ActivityProvider> providers;

    /**
     * Constructor with dependency injection.
     * Spring automatically injects all beans implementing ActivityProvider interface
     * into this list, making the system extensible without code modification.
     *
     * @param providers all registered map item providers
     */
    public WazaiSearchService(List<ActivityProvider> providers) {
        this.providers = providers;
    }

    /**
     * Search all providers for map items matching the keyword.
     * Results from all providers are combined into a single list.
     *
     * @param keyword the search term
     * @return unified list of map items (events and places) from all providers
     */
    public List<WazaiMapItem> searchAll(String keyword) {
        return searchAll(keyword, "ALL");
    }

    /**
     * Search all providers for map items matching the keyword and country filter.
     *
     * @param keyword the search term
     * @param countryCode country filter: "TW", "JP", or "ALL"
     * @return filtered list of map items matching the country criteria
     */
    public List<WazaiMapItem> searchAll(String keyword, String countryCode) {
        List<WazaiMapItem> allResults = providers.stream()
                .map(provider -> searchSingleProvider(provider, keyword))
                .flatMap(List::stream)
                .toList();

        return filterByCountry(allResults, countryCode);
    }

    /**
     * Get all registered provider names.
     *
     * @return list of provider names
     */
    public List<String> getProviderNames() {
        return providers.stream()
                .map(ActivityProvider::getProviderName)
                .toList();
    }

    private List<WazaiMapItem> searchSingleProvider(ActivityProvider provider, String keyword) {
        try {
            return provider.search(keyword);
        } catch (Exception e) {
            // Log the error but don't fail the entire search
            // TODO: Add proper logging
            return List.of();
        }
    }

    /**
     * Filter map items by country code.
     *
     * @param items list of map items to filter
     * @param countryCode country filter: "TW", "JP", or "ALL"
     * @return filtered list
     */
    private List<WazaiMapItem> filterByCountry(List<WazaiMapItem> items, String countryCode) {
        if (countryCode == null || countryCode.equalsIgnoreCase("ALL")) {
            return items;
        }

        Country targetCountry = parseCountryCode(countryCode);
        if (targetCountry == null) {
            return items; // Invalid country code, return all
        }

        return items.stream()
                .filter(item -> item.country() == targetCountry)
                .toList();
    }

    /**
     * Parse country code string to Country enum.
     *
     * @param countryCode "TW", "JP", etc.
     * @return corresponding Country enum, or null if invalid
     */
    private Country parseCountryCode(String countryCode) {
        return switch (countryCode.toUpperCase()) {
            case "TW" -> Country.TAIWAN;
            case "JP" -> Country.JAPAN;
            default -> null;
        };
    }
}
