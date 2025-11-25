package com.oni.masks.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oni.masks.OniMasksPlugin;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class PlayerDataManager {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final Map<UUID, PlayerData> playerDataCache = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public PlayerData getPlayerData(final UUID playerId) {
        return this.playerDataCache.computeIfAbsent(playerId, this::loadPlayerData);
    }
    
    private PlayerData loadPlayerData(final UUID playerId) {
        final File dataFile = this.getPlayerDataFile(playerId);
        
        if (!dataFile.exists()) {
            return new PlayerData(playerId);
        }
        
        try (FileReader reader = new FileReader(dataFile)) {
            final PlayerData data = this.gson.fromJson(reader, PlayerData.class);
            return data != null ? data : new PlayerData(playerId);
        } catch (IOException exception) {
            this.plugin.getLogger().warning("Failed to load player data for " + playerId + ": " + exception.getMessage());
            return new PlayerData(playerId);
        }
    }
    
    public void savePlayerData(final UUID playerId) {
        final PlayerData playerData = this.playerDataCache.get(playerId);
        if (playerData == null) {
            return;
        }
        
        final File dataFile = this.getPlayerDataFile(playerId);
        dataFile.getParentFile().mkdirs();
        
        try (FileWriter writer = new FileWriter(dataFile)) {
            this.gson.toJson(playerData, writer);
        } catch (IOException exception) {
            this.plugin.getLogger().severe("Failed to save player data for " + playerId + ": " + exception.getMessage());
        }
    }
    
    public void saveAllPlayerData() {
        for (final UUID playerId : this.playerDataCache.keySet()) {
            this.savePlayerData(playerId);
        }
    }
    
    public void removePlayerData(final UUID playerId) {
        this.playerDataCache.remove(playerId);
    }
    
    private File getPlayerDataFile(final UUID playerId) {
        return new File(this.plugin.getDataFolder(), "playerdata/" + playerId.toString() + ".json");
    }
}