package dev.koukeneko.wazai.service;

import dev.koukeneko.wazai.dto.WazaiActivity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Aggregator service that coordinates multiple activity providers.
 * This service acts as a facade, delegating search requests to all registered
 * providers and combining their results into a unified response.
 */
@Service
public class WazaiSearchService {

    private final List<ActivityProvider> providers;

    /**
     * Constructor with dependency injection.
     * Spring automatically injects all beans implementing ActivityProvider interface
     * into this list, making the system extensible without code modification.
     *
     * @param providers all registered activity providers
     */
    public WazaiSearchService(List<ActivityProvider> providers) {
        this.providers = providers;
    }

    /**
     * Search all providers for activities matching the keyword.
     * Results from all providers are combined into a single list.
     *
     * @param keyword the search term
     * @return unified list of activities from all providers
     */
    public List<WazaiActivity> searchAll(String keyword) {
        return providers.stream()
                .map(provider -> searchSingleProvider(provider, keyword))
                .flatMap(List::stream)
                .toList();
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

    private List<WazaiActivity> searchSingleProvider(ActivityProvider provider, String keyword) {
        try {
            return provider.search(keyword);
        } catch (Exception e) {
            // Log the error but don't fail the entire search
            // TODO: Add proper logging
            return List.of();
        }
    }
}
