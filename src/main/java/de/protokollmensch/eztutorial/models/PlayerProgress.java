package de.protokollmensch.eztutorial.models;

import java.util.UUID;

public class PlayerProgress {
    private final UUID uniqueId;
    private boolean prompted;
    private boolean active;
    private boolean completed;
    private int currentTaskIndex;

    public PlayerProgress(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.prompted = false;
        this.active = false;
        this.completed = false;
        this.currentTaskIndex = 0;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public boolean isPrompted() {
        return prompted;
    }

    public void setPrompted(boolean prompted) {
        this.prompted = prompted;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getCurrentTaskIndex() {
        return currentTaskIndex;
    }

    public void setCurrentTaskIndex(int currentTaskIndex) {
        this.currentTaskIndex = Math.max(0, currentTaskIndex);
    }
}

