package dev.koukeneko.wazai.dto.external.aws;

import java.util.List;

/**
 * Wrapper for a single AWS Summit event containing the item and its tags.
 */
public record AwsEventWrapper(
        AwsEventItem item,
        List<AwsTag> tags
) {
}
