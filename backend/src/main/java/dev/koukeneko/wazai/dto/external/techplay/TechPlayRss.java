package dev.koukeneko.wazai.dto.external.techplay;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "rss")
public record TechPlayRss(
        @JacksonXmlProperty(localName = "channel")
        TechPlayChannel channel
) {}
