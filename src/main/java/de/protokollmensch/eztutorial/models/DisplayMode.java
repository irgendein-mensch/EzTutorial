package de.protokollmensch.eztutorial.models;

public enum DisplayMode {
    CHAT,
    ACTIONBAR,
    TITLE,
    SCOREBOARD;

    public static DisplayMode fromString(String value, DisplayMode fallback) {
        if (value == null) {
            return fallback;
        }

        try {
            return DisplayMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}

