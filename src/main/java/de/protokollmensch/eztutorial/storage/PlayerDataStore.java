package de.protokollmensch.eztutorial.storage;

import de.protokollmensch.eztutorial.models.PlayerProgress;
import java.util.UUID;

public interface PlayerDataStore {

    PlayerProgress getOrCreate(UUID uniqueId);

    void save(PlayerProgress progress);

    void reset(UUID uniqueId);

    void saveAll();

    default void close() {
    }
}

