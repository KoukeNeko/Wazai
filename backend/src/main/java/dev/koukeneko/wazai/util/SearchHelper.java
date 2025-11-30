package dev.koukeneko.wazai.util;

import dev.koukeneko.wazai.dto.WazaiMapItem;

public class SearchHelper {

    private SearchHelper() {
        // Utility class
    }

    /**
     * Checks if the given map item matches the keyword.
     * The match is case-insensitive and checks the title, description, and ID.
     *
     * @param item the map item to check
     * @param keyword the keyword to search for
     * @return true if the item matches the keyword, false otherwise
     */
    public static boolean matchesKeyword(WazaiMapItem item, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String lowerKeyword = keyword.toLowerCase();

        if (item.title() != null && item.title().toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        if (item.description() != null && item.description().toLowerCase().contains(lowerKeyword)) {
            return true;
        }
        
        if (item.id() != null && item.id().toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        return false;
    }
}
