package dev.koukeneko.wazai.dto.external.techplay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Represents an event item from TechPlay RSS feed.
 *
 * TechPlay uses custom namespace "tp:" for event-specific fields.
 * Jackson XML handles namespaced elements by local name when namespace-aware parsing is disabled.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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

        @JacksonXmlProperty(localName = "eventDate")
        String eventDate,

        @JacksonXmlProperty(localName = "eventStartTime")
        String eventStartTime,

        @JacksonXmlProperty(localName = "eventEndTime")
        String eventEndTime,

        @JacksonXmlProperty(localName = "eventPlace")
        String eventPlace,

        @JacksonXmlProperty(localName = "eventAddress")
        String eventAddress,

        @JacksonXmlProperty(localName = "creator")
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
