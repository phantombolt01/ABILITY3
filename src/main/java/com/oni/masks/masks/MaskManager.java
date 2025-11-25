package com.oni.masks.masks;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.masks.types.EarthMask;
import com.oni.masks.masks.types.FlameMask;
import com.oni.masks.masks.types.LightMask;
import com.oni.masks.masks.types.LightningMask;
import com.oni.masks.masks.types.WaterMask;
import com.oni.masks.masks.types.ForbiddenShadowsMask;
import com.oni.masks.masks.types.PrimordialFlameMask;
import com.oni.masks.masks.types.VoidMask;
import com.oni.masks.player.PlayerData;
import com.oni.masks.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Random;

public class MaskManager {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
    private final Random random = new Random();
    
    public Mask createMask(final Player player, final MaskType maskType) {
        return switch (maskType) {
            case FLAME -> new FlameMask(player);
            case EARTH -> new EarthMask(player);
            case WATER -> new WaterMask(player);
            case LIGHT -> new LightMask(player);
            case VOID -> new VoidMask(player);
            case LIGHTNING -> new LightningMask(player);
            case FORBIDDEN_SHADOWS -> new ForbiddenShadowsMask(player);
            case PRIMORDIAL_FLAME -> new PrimordialFlameMask(player);
        };
    }
    
    public void assignRandomMask(final Player player) {
        // Include Void and Lightning in regular mask pool
        final MaskType[] regularMasks = {
            MaskType.FLAME, MaskType.EARTH, MaskType.WATER, 
            MaskType.LIGHT, MaskType.VOID, MaskType.LIGHTNING
        };
        final MaskType randomMaskType = regularMasks[this.random.nextInt(regularMasks.length)];
        this.assignMask(player, randomMaskType);
    }
    
    public void assignEventMask(final Player player, final MaskType eventMaskType) {
        if (!eventMaskType.isEventMask()) {
            throw new IllegalArgumentException("Can only assign event masks through this method");
        }
        
        final PlayerData playerData = this.playerDataManager.getPlayerData(player.getUniqueId());
        
        // Store current mask before switching to event mask
        if (playerData.getCurrentMask() != null && !playerData.getMaskType().isEventMask()) {
            playerData.setPreviousMaskType(playerData.getMaskType());
        }
        
        // Remove current mask effects
        if (playerData.getCurrentMask() != null) {
            playerData.getCurrentMask().removePassiveEffects();
        }
        
        // Create and assign event mask
        final Mask eventMask = this.createMask(player, eventMaskType);
        playerData.setCurrentMask(eventMask);
        playerData.setMaskType(eventMaskType);
        
        // Apply event mask effects
        eventMask.applyPassiveEffects();
        eventMask.onEquip();
        
        // Send announcement message
        final Component message = eventMaskType.getAnnouncementMessage();
        player.sendMessage(message);
        
        // Play special event mask sound
        if (eventMaskType == MaskType.FORBIDDEN_SHADOWS) {
            this.plugin.getSoundManager().playEventMaskActivation();
        } else if (eventMaskType == MaskType.PRIMORDIAL_FLAME) {
            this.plugin.getSoundManager().playPrimordialFlameActivation();
        }
        
        // Save player data
        this.playerDataManager.savePlayerData(player.getUniqueId());
    }
    
    public void assignMask(final Player player, final MaskType maskType) {
        final PlayerData playerData = this.playerDataManager.getPlayerData(player.getUniqueId());
        
        // Remove current mask effects if any
        if (playerData.getCurrentMask() != null) {
            playerData.getCurrentMask().removePassiveEffects();
        }
        
        // Create and assign new mask
        final Mask newMask = this.createMask(player, maskType);
        playerData.setCurrentMask(newMask);
        playerData.setMaskType(maskType);
        
        // Apply passive effects
        newMask.applyPassiveEffects();
        newMask.onEquip();
        
        // Send announcement message
        final Component message = maskType.getAnnouncementMessage();
        player.sendMessage(message);
        
        // Play sound effect
        this.plugin.getSoundManager().playMaskAssignSound(player);
        
        // Save player data
        this.playerDataManager.savePlayerData(player.getUniqueId());
    }
    
    public void rerollMask(final Player player) {
        final PlayerData playerData = this.playerDataManager.getPlayerData(player.getUniqueId());
        final MaskType currentMaskType = playerData.getMaskType();
        
        // Cannot reroll event masks
        if (currentMaskType != null && currentMaskType.isEventMask()) {
            player.sendMessage(Component.text("Event masks cannot be rerolled!", NamedTextColor.RED));
            return;
        }
        
        MaskType newMaskType;
        do {
            // Include Void and Lightning in reroll pool
            final MaskType[] regularMasks = {
                MaskType.FLAME, MaskType.EARTH, MaskType.WATER, 
                MaskType.LIGHT, MaskType.VOID, MaskType.LIGHTNING
            };
            newMaskType = regularMasks[this.random.nextInt(regularMasks.length)];
        } while (newMaskType == currentMaskType);
        
        this.assignMask(player, newMaskType);
        
        final Component rerollMessage = Component.text("You have rerolled your mask into ")
                .append(newMaskType.getFormattedName())
                .append(Component.text("!"));
        player.sendMessage(rerollMessage);
    }
}