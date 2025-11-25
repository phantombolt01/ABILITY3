package com.oni.masks.effects;

import com.oni.masks.OniMasksPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class SoundManager {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public void playMaskAssignSound(final Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
    }
    
    public void playFireballSound(final Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.8f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 0.5f, 1.0f);
    }
    
    public void playEarthSpikeSound(final Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_DRIPSTONE_BLOCK_BREAK, 1.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.BLOCK_POINTED_DRIPSTONE_LAND, 1.0f, 0.6f);
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 0.8f, 0.5f);
    }
    
    public void playEarthShieldSound(final Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 0.8f, 0.8f);
    }
    
    public void playWaterWaveSound(final Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 1.2f, 0.8f);
        player.playSound(player.getLocation(), Sound.ITEM_BUCKET_EMPTY, 0.8f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 1.0f, 1.0f);
    }
    
    public void playHealingSound(final Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.3f);
    }
    
    public void playLightBallSound(final Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.8f);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.5f);
    }
    
    public void playLightImpactSound(final Location location) {
        location.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 1.5f);
        location.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 0.7f, 1.2f);
    }
    
    public void playTrustSound(final Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.0f);
    }
    
    public void playUntrustSound(final Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.5f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 0.8f);
    }
    
    public void playEventMaskActivation() {
        // Broadcast wither spawn sound
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.0f);
        }
    }
    
    public void playPrimordialFlameActivation() {
        // Broadcast massive flame ignition sounds
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.5f, 0.8f);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
        }
    }
    
    public void playPrimordialFlameOrb(final Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.0f);
        player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 2.0f, 0.5f);
    }
    
    public void playRerollSound(final Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
    }
    
    // Void Mask Sounds
    public void playVoidActivation() {
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
            player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 0.8f, 0.8f);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.6f, 0.7f);
        }
    }
    
    public void playVoidGraspSound(final Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.8f);
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 0.6f);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 0.8f, 1.2f);
    }
    
    public void playVoidEruptionSound(final Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.5f, 0.7f);
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 0.8f);
    }
    
    // Lightning Mask Sounds
    public void playLightningActivation() {
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.8f, 1.2f);
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.6f, 1.5f);
        }
    }
    
    public void playStormCallSound(final Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.WEATHER_RAIN_ABOVE, 1.5f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.6f);
    }
    
    public void playLightningBurstSound(final Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.8f);
    }
}