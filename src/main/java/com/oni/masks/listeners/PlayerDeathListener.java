package com.oni.masks.listeners;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.config.PluginConfig;
import com.oni.masks.player.PlayerData;
import com.oni.masks.player.PlayerDataManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.oni.masks.masks.MaskType;
import org.bukkit.event.entity.PlayerDeathEvent;

@RequiredArgsConstructor
public class PlayerDeathListener implements Listener {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
    
    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player victim = event.getEntity();
        final Player killer = victim.getKiller();
        
        // Handle Event Mask inheritance
        if (killer != null && !killer.equals(victim)) {
            this.handleEventMaskInheritance(killer, victim);
        }
        
        // Handle kill tracking for upgrades
        if (killer != null && !killer.equals(victim)) {
            this.handlePlayerKill(killer, victim);
        }
        
        // Handle upgrade reset on death if configured
        final PluginConfig config = this.plugin.getConfigManager().getPluginConfig();
        if (config.isResetUpgradesOnDeath()) {
            final PlayerData victimData = this.playerDataManager.getPlayerData(victim.getUniqueId());
            if (victimData.getPlayerKills() > 0) {
                victimData.setPlayerKills(0);
                victimData.setMaskTier(0);
                
                victim.sendMessage(Component.text("Your mask upgrades have been reset due to death!", NamedTextColor.RED));
                
                // Reapply mask effects with new tier
                if (victimData.getCurrentMask() != null) {
                    victimData.getCurrentMask().removePassiveEffects();
                    victimData.getCurrentMask().applyPassiveEffects();
                }
                
                this.playerDataManager.savePlayerData(victim.getUniqueId());
            }
        }
    }
    
    private void handleEventMaskInheritance(final Player killer, final Player victim) {
        final PlayerData victimData = this.playerDataManager.getPlayerData(victim.getUniqueId());
        
        // Check if victim had an event mask
        if (victimData.getMaskType().isEventMask()) {
            final MaskType eventMaskType = victimData.getMaskType();
            
            // Transfer event mask to killer
            this.plugin.getMaskManager().assignEventMask(killer, eventMaskType);
            
            // Assign random normal mask to victim
            this.plugin.getMaskManager().assignRandomMask(victim);
            
            // Send inheritance messages
            final Component killerMessage = this.getEventMaskInheritanceMessage(eventMaskType);
            killer.sendMessage(killerMessage);
            
            victim.sendMessage(Component.text("§8[Event] §7The Event Mask has left your grasp.", NamedTextColor.GRAY));
            
            // Save data
            this.playerDataManager.savePlayerData(killer.getUniqueId());
            this.playerDataManager.savePlayerData(victim.getUniqueId());
        }
    }
    
    private Component getEventMaskInheritanceMessage(final MaskType maskType) {
        return switch (maskType) {
            case FORBIDDEN_SHADOWS -> Component.text("§5[Forbidden Shadows] §7You have claimed the Event Mask by slaying its bearer. §7The shadows now bind themselves to you...");
            case PRIMORDIAL_FLAME -> Component.text("§6[Primordial Flame] §7You have seized the Event Mask from your enemy's ashes. §7The flames roar within you now.");
            case VOID -> Component.text("§8[Void] §7You have claimed the void from the fallen. §7The abyss recognizes a new master.");
            case LIGHTNING -> Component.text("§e[Lightning] §7You have seized the storm from your enemy. §7Thunder bows to your will.");
            default -> Component.text("§7You have claimed an Event Mask from the fallen.");
        };
    }
    
    private void handlePlayerKill(final Player killer, final Player victim) {
        final PlayerData killerData = this.playerDataManager.getPlayerData(killer.getUniqueId());
        
        // Add unique kill and check for tier upgrade
        final boolean tierUpgraded = killerData.addUniqueKill(victim.getUniqueId());
        final int uniqueKills = killerData.getUniqueKillCount();
        final int newTier = killerData.getTierLevel();
        
        if (tierUpgraded) {
            // Update mask tier to match kill tier
            killerData.setMaskTier(newTier);
            
            // Send upgrade message
            final Component upgradeMessage = Component.text("⚡ ", NamedTextColor.GOLD)
                    .append(Component.text("MASK UPGRADE! ", NamedTextColor.GOLD))
                    .append(Component.text("Tier " + newTier + " unlocked!", NamedTextColor.YELLOW));
            
            killer.sendMessage(upgradeMessage);
            
            // Play upgrade sound and particles
            this.plugin.getSoundManager().playMaskAssignSound(killer);
            this.plugin.getParticleManager().playEventActivationParticles(killer.getLocation());
            
            // Reapply mask effects with new tier
            if (killerData.getCurrentMask() != null) {
                killerData.getCurrentMask().removePassiveEffects();
                killerData.getCurrentMask().applyPassiveEffects();
            }
        }
        
        // Send kill confirmation
        killer.sendMessage(Component.text("+" + uniqueKills + " unique kills", NamedTextColor.GREEN)
                .append(Component.text(" (Tier " + newTier + ")", NamedTextColor.GRAY)));
        
        this.playerDataManager.savePlayerData(killer.getUniqueId());
    }
}