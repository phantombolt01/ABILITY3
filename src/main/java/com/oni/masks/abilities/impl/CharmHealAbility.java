package com.oni.masks.abilities.impl;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CharmHealAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public CharmHealAbility(final Player player) {
        super("Charm Heal", player, 30);
    }
    
    @Override
    public void execute() {
        final Location playerLocation = this.player.getLocation();
        
        // Heal player for 5 hearts (10 HP)
        final double newHealth = Math.min(this.player.getMaxHealth(), this.player.getHealth() + 10.0);
        this.player.setHealth(newHealth);
        
        // Pink healing particles
        playerLocation.getWorld().spawnParticle(org.bukkit.Particle.HEART, 
                playerLocation.add(0, 2, 0), 15, 1, 1, 1, 0.1);
        playerLocation.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, 
                playerLocation, 10, 0.5, 0.5, 0.5, 0.05);
        
        // Soft pink dust waves
        for (int i = 0; i < 3; i++) {
            final double radius = 1 + i;
            for (int j = 0; j < 16; j++) {
                final double angle = (2 * Math.PI * j / 16);
                final double x = radius * Math.cos(angle);
                final double z = radius * Math.sin(angle);
                
                final Location waveLocation = playerLocation.clone().add(x, 0.5, z);
                playerLocation.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, 
                        waveLocation, 1, 0.1, 0.1, 0.1, 0.02);
            }
        }
        
        // Play charming sounds
        this.player.getWorld().playSound(playerLocation, org.bukkit.Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
        this.player.getWorld().playSound(playerLocation, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.8f);
        
        this.sendAbilityMessage("§d§l[LUST] §7Crimson desire mends your wounds!");
    }
}