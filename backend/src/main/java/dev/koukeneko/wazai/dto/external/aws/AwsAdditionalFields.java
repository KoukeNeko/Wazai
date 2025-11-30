package dev.koukeneko.wazai.dto.external.aws;

/**
 * Additional fields for AWS Summit event containing detailed event information.
 */
public record AwsAdditionalFields(
        String badge,
        String body,
        String bodyBack,
        String ctaLabel,
        String ctaLink,
        String date,
        String durationMinutes,
        String heading,
        String publishedDate,
        String time,
        String title
) {
}
