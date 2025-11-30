package dev.koukeneko.wazai.dto.external;

import java.util.List;

/**
 * Response format from Connpass API v2.
 * This record represents the external API response structure,
 * which will be transformed into WazaiActivity format for internal use.
 */
public record ConnpassResponse(
        int results_returned,
        List<ConnpassEvent> events
) {}
