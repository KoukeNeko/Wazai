package dev.koukeneko.wazai.controller;

import dev.koukeneko.wazai.dto.WazaiMapItem;
import dev.koukeneko.wazai.service.WazaiSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for searching map items (events and places) across all providers.
 * This controller serves as the primary entry point for frontend requests,
 * delegating all search operations to the WazaiSearchService.
 *
 * Results can include both time-based events (WazaiEvent) and static locations (WazaiPlace).
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final WazaiSearchService searchService;

    public SearchController(WazaiSearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Search for map items (events and places) across all registered providers.
     *
     * @param keyword the search term (required)
     * @return list of map items from all providers in unified format
     */
    @GetMapping
    public List<WazaiMapItem> searchMapItems(@RequestParam String keyword) {
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
