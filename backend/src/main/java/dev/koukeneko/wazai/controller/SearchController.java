package dev.koukeneko.wazai.controller;

import dev.koukeneko.wazai.dto.WazaiMapItem;
import dev.koukeneko.wazai.service.WazaiSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Search", description = "Search for events and places across multiple data sources")
public class SearchController {

    private final WazaiSearchService searchService;

    public SearchController(WazaiSearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Search for map items (events and places) across all registered providers.
     *
     * @param keyword the search term (optional, returns all items if omitted)
     * @return list of map items from all providers in unified format
     */
    @GetMapping
    @Operation(
            summary = "Search for events and places",
            description = """
                    Search across all registered providers for events and places matching the keyword.

                    **Keyword:**
                    - If provided: Filters results by the keyword (case-insensitive).
                    - If omitted: Returns all available events/places from the providers.

                    **Supported Keywords:**
                    - Technology: `python`, `javascript`, `java`, `security`, `開源`
                    - Event Types: `conference`, `workshop`, `meetup`, `devfest`
                    - Communities: `gdg`, `google`, `pycon`, `mopcon`, `coscup`
                    - Locations: `taiwan`, `taipei`, `kaohsiung`, `taichung`, `hsinchu`
                    - Generic: `tech`, `活動`

                    **Country Filtering:**
                    - `ALL` (default): Returns events from all countries
                    - `TW`: Returns only Taiwan events
                    - `JP`: Returns only Japan events
                    
                    **Provider Filtering:**
                    - `ALL` (default): Search all providers
                    - Specific name (case-insensitive partial match): e.g., `connpass`, `taiwan`

                    **Returns:**
                    - Events with start/end times (conferences, meetups)
                    - Places with business hours (clinics, cafes)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved search results",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = WazaiMapItem.class),
                            examples = @ExampleObject(
                                    name = "PyCon Taiwan Example",
                                    value = """
                                            [{
                                              "id": "pycon-tw-2026",
                                              "title": "PyCon Taiwan 2026",
                                              "description": "台灣最大的 Python 年會",
                                              "url": "https://tw.pycon.org",
                                              "coordinates": {
                                                "latitude": 25.033,
                                                "longitude": 121.5654
                                              },
                                              "startTime": "2026-09-04T09:00:00",
                                              "endTime": "2026-09-06T18:00:00",
                                              "eventType": "CONFERENCE",
                                              "source": "TAIWAN_TECH_COMMUNITY",
                                              "country": "TAIWAN"
                                            }]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search parameters"
            )
    })
    public List<WazaiMapItem> searchMapItems(
            @Parameter(
                    description = "Search keyword (supports English and Chinese). If omitted, returns all items.",
                    example = "python",
                    required = false
            )
            @RequestParam(required = false) String keyword,
            @Parameter(
                    description = "Country filter: TW (Taiwan), JP (Japan), or ALL (default)",
                    example = "ALL"
            )
            @RequestParam(defaultValue = "ALL") String country,
            @Parameter(
                    description = "Provider filter: Partial match on provider name (e.g. 'Connpass'), or ALL (default)",
                    example = "ALL"
            )
            @RequestParam(defaultValue = "ALL") String provider
    ) {
        return searchService.searchAll(keyword, country, provider);
    }

    /**
     * Get information about available data providers.
     *
     * @return map containing provider names
     */
    @GetMapping("/providers")
    @Operation(
            summary = "Get available data providers",
            description = "Returns a list of all registered data providers in the system"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved provider list",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "providers": [
                                                "Connpass",
                                                "Taiwan Tech Community"
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    public Map<String, List<String>> getProviders() {
        return Map.of("providers", searchService.getProviderNames());
    }
}
