package dev.koukeneko.wazai.dto.external.techplay;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Represents an event item from TechPlay RSS feed.
 *
 * TechPlay uses custom namespace "tp:" for event-specific fields:
 * - tp:eventDate: Event date (YYYY-MM-DD)
 * - tp:eventStartTime: Start timestamp
 * - tp:eventEndTime: End timestamp
 * - tp:eventPlace: Venue name
 * - tp:eventAddress: Full address
 */
public record TechPlayItem(
        @JacksonXmlProperty(localName = "title")
        String title,

        @JacksonXmlProperty(localName = "link")
        String link,

        @JacksonXmlProperty(localName = "guid")
        String guid,

        @JacksonXmlProperty(localName = "description")
        String description,

        @JacksonXmlProperty(localName = "pubDate")
        String pubDate,

        @JacksonXmlProperty(localName = "eventDate", namespace = "https://techplay.jp/")
        String eventDate,

        @JacksonXmlProperty(localName = "eventStartTime", namespace = "https://techplay.jp/")
        String eventStartTime,

        @JacksonXmlProperty(localName = "eventEndTime", namespace = "https://techplay.jp/")
        String eventEndTime,

        @JacksonXmlProperty(localName = "eventPlace", namespace = "https://techplay.jp/")
        String eventPlace,

        @JacksonXmlProperty(localName = "eventAddress", namespace = "https://techplay.jp/")
        String eventAddress,

        @JacksonXmlProperty(localName = "creator", namespace = "http://purl.org/dc/elements/1.1/")
        String creator
) {
    /**
     * Extracts the event ID from the link URL.
     * Example: https://techplay.jp/event/986053 -> 986053
     */
    public String extractEventId() {
        if (link == null || link.isBlank()) {
            return guid;
        }
        int lastSlash = link.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < link.length() - 1) {
            return link.substring(lastSlash + 1);
        }
        return guid;
    }
}
