package com.oni.masks.sins;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.player.PlayerData;
import com.oni.masks.player.PlayerDataManager;
import com.oni.masks.sins.types.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class SinManager {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
    
    public Sin createSin(final Player player, final SinType sinType) {
        return switch (sinType) {
            case PRIDE -> new PrideSin(player);
            case WRATH -> new WrathSin(player);
            case ENVY -> new EnvySin(player);
            case GREED -> new GreedSin(player);
            case LUST -> new LustSin(player);
            case GLUTTONY -> new GluttonySin(player);
            case SLOTH -> new SlothSin(player);
        };
    }
    
    public void assignSin(final Player player, final SinType sinType) {
        final PlayerData playerData = this.playerDataManager.getPlayerData(player.getUniqueId());
        
        // Remove current mask/sin effects if any
        if (playerData.getCurrentMask() != null) {
            playerData.getCurrentMask().removePassiveEffects();
        }
        if (playerData.getCurrentSin() != null) {
            playerData.getCurrentSin().removePassiveEffects();
        }
        
        // Create and assign new sin
        final Sin newSin = this.createSin(player, sinType);
        playerData.setCurrentSin(newSin);
        playerData.setSinType(sinType);
        
        // Set max health based on sin
        final double maxHealth = sinType.getHearts() * 2.0; // Convert hearts to health points
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            player.setHealth(Math.min(player.getHealth(), maxHealth));
        }
        
        // Apply passive effects
        newSin.applyPassiveEffects();
        newSin.onEquip();
        
        // Send announcement message
        final Component message = sinType.getAnnouncementMessage();
        player.sendMessage(message);
        
        // Play sound effect
        this.plugin.getSoundManager().playMaskAssignSound(player);
        
        // Save player data
        this.playerDataManager.savePlayerData(player.getUniqueId());
    }
    
    public void removeSin(final Player player) {
        final PlayerData playerData = this.playerDataManager.getPlayerData(player.getUniqueId());
        
        if (playerData.getCurrentSin() != null) {
            playerData.getCurrentSin().removePassiveEffects();
            playerData.setCurrentSin(null);
            playerData.setSinType(null);
            
            // Reset max health to default
            if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
                player.setHealth(Math.min(player.getHealth(), 20.0));
            }
            
            player.sendMessage(Component.text("The sin has been lifted from your soul.", NamedTextColor.GREEN));
            this.playerDataManager.savePlayerData(player.getUniqueId());
        }
    }
}