package de.protokollmensch.eztutorial.models;

public enum TaskType {
    MOVE_TO,
    COMMAND;

    public static TaskType fromString(String value) {
        if (value == null) {
            return MOVE_TO;
        }

        try {
            return TaskType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return MOVE_TO;
        }
    }
}

