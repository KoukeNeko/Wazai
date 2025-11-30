package dev.koukeneko.wazai.dto.external.meetup;

public record MeetupEvent(
    String id,
    String title,
    String shortDescription,
    String eventUrl,
    String dateTime, // ISO string
    MeetupVenue venue,
    MeetupGroup group
) {}
