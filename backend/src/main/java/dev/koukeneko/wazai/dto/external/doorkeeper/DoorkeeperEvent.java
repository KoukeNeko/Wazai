package dev.koukeneko.wazai.dto.external.doorkeeper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Doorkeeper API event object.
 *
 * @see <a href="https://www.doorkeeper.jp/developer/api">Doorkeeper API Documentation</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DoorkeeperEvent(
        long id,
        String title,
        String description,
        @JsonProperty("public_url") String publicUrl,
        @JsonProperty("starts_at") String startsAt,
        @JsonProperty("ends_at") String endsAt,
        @JsonProperty("venue_name") String venueName,
        String address,
        String lat,
        @JsonProperty("long") String lng,
        @JsonProperty("ticket_limit") Integer ticketLimit,
        Integer participants,
        Integer waitlisted,
        @JsonProperty("published_at") String publishedAt,
        @JsonProperty("updated_at") String updatedAt,
        Integer group
) {}
