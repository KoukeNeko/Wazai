package dev.koukeneko.wazai.controller;

import dev.koukeneko.wazai.dto.WazaiActivity;
import dev.koukeneko.wazai.service.WazaiSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for searching activities across all providers.
 * This controller serves as the primary entry point for frontend requests,
 * delegating all search operations to the WazaiSearchService.
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final WazaiSearchService searchService;

    public SearchController(WazaiSearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Search for activities across all registered providers.
     *
     * @param keyword the search term (required)
     * @return list of activities from all providers in unified format
     */
    @GetMapping
    public List<WazaiActivity> searchActivities(@RequestParam String keyword) {
        return searchService.searchAll(keyword);
    }

    /**
     * Get information about available data providers.
     *
     * @return map containing provider names
     */
    @GetMapping("/providers")
    public Map<String, List<String>> getProviders() {
        return Map.of("providers", searchService.getProviderNames());
    }
}
