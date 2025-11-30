package dev.koukeneko.wazai.dto.external.gdg;

import java.util.List;

/**
 * Represents a geographic region containing multiple GDG chapters.
 * Response from /api/chapter_region?chapters=true endpoint.
 */
public record GdgRegion(
        Long id,
        Integer order,
        String title,
        List<GdgChapterInfo> chapters
) {
}
