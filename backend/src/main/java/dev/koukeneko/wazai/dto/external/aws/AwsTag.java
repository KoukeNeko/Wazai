package dev.koukeneko.wazai.dto.external.aws;

/**
 * Tag information for AWS Summit events indicating geographic regions.
 */
public record AwsTag(
        String id,
        String locale,
        String tagNamespaceId,
        String name,
        String description,
        String dateCreated,
        String dateUpdated
) {
}
