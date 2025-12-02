package dev.koukeneko.wazai.dto.external.aws;

/**
 * Additional fields for AWS events containing detailed event information.
 * Supports both AWS Summit and AWS Community Day events.
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
        String location,        // Community Day events
        String publishedDate,
        String time,            // Summit events
        String title
) {
}
