package dev.koukeneko.wazai.dto.external.aws;

import java.util.List;
import java.util.Map;

/**
 * Root response from AWS Summit events API.
 */
public record AwsApiResponse(
        Map<String, Object> fieldTypes,
        List<AwsEventWrapper> items,
        Map<String, Object> metadata
) {
}
