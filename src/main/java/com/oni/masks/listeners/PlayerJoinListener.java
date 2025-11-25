package com.oni.masks.listeners;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.player.PlayerData;
import com.oni.masks.player.PlayerDataManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
    
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PlayerData playerData = this.playerDataManager.getPlayerData(player.getUniqueId());
        
        // Check if this is the player's first time joining
        if (!playerData.isHasJoinedBefore()) {
            playerData.setHasJoinedBefore(true);
            
            // Assign random mask after a small delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getMaskManager().assignRandomMask(player);
                }
            }.runTaskLater(this.plugin, 20L); // 1 second delay
        } else {
            // Player has joined before, restore their mask
            if (playerData.getMaskType() != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getMaskManager().assignMask(player, playerData.getMaskType());
                    }
                }.runTaskLater(this.plugin, 20L);
            }
        }
    }
}