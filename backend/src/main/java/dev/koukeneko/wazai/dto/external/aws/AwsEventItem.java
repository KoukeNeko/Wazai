package dev.koukeneko.wazai.dto.external.aws;

/**
 * AWS Summit event item containing basic metadata and detailed information.
 */
public record AwsEventItem(
        String id,
        String locale,
        String directoryId,
        String name,
        String dateCreated,
        String dateUpdated,
        AwsAdditionalFields additionalFields
) {
}
