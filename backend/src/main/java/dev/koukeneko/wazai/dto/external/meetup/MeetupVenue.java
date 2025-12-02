package dev.koukeneko.wazai.dto.external.meetup;

public record MeetupVenue(
    String name,
    String address,
    String city,
    Double lat,
    Double lon
) {}
