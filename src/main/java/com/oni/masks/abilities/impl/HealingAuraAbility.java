package com.oni.masks.abilities.impl;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.player.DamageUtils;
import com.oni.masks.config.PluginConfig;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HealingAuraAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PluginConfig config = this.plugin.getConfigManager().getPluginConfig();
    
    public HealingAuraAbility(final Player player) {
        super("Healing Aura", player, 90);
    }
    
    @Override
    public void execute() {
        final Location playerLocation = this.player.getLocation();
        
        // Visual and audio effects
        this.plugin.getParticleManager().playHealingAuraAnimation(this.player);
        this.plugin.getSoundManager().playHealingSound(this.player);
        
        // Find trusted players to heal (mass targeting for healing)
        final List<Player> trustedTargets = this.findTrustedPlayers(playerLocation, 12.0);
        final int maxTargets = Math.min(trustedTargets.size(), this.config.getMaxTargetsPerAbility());
        
        // Start healing over time effect
        new BukkitRunnable() {
            private int ticks = 0;
            private int processed = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 100) { // 5 seconds
                    this.cancel();
                    return;
                }
                
                // Process healing in batches
                if (this.ticks % 5 == 0) { // Every 5 ticks (0.25 seconds)
                    final int batchSize = Math.min(config.getBatchSize(), maxTargets - processed);
                    final int endIndex = Math.min(processed + batchSize, maxTargets);
                    
                    for (int i = processed; i < endIndex; i++) {
                        final Player targetPlayer = trustedTargets.get(i);
                        
                        // Heal 5 hearts total per cast
                        final double currentHealth = targetPlayer.getHealth();
                        final double maxHealth = targetPlayer.getMaxHealth();
                        final double newHealth = Math.min(maxHealth, currentHealth + 10.0); // 5 hearts = 10 HP
                        targetPlayer.setHealth(newHealth);
                            
                            // Play healing particles on target
                            plugin.getParticleManager().playHealingParticles(targetPlayer.getLocation());
                    }
                    
                    processed = endIndex;
                    if (processed >= maxTargets) {
                        processed = 0; // Reset for next cycle
                    }
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L); // Every tick
        
        this.sendAbilityMessage("§e§l[Light Mask] §7Healing light embraces " + maxTargets + " trusted allies!");
    }
    
    private List<Player> findTrustedPlayers(final Location center, final double radius) {
        final List<Player> trustedPlayers = new ArrayList<>();
        
        // Always include self
        trustedPlayers.add(this.player);
        
        for (final Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof Player && !entity.equals(this.player)) {
                final Player targetPlayer = (Player) entity;
                
                // Only heal trusted players
                if (this.plugin.getTrustManager().canHelp(this.player, targetPlayer)) {
                    trustedPlayers.add(targetPlayer);
                }
            }
        }
        
        // Sort by distance (closest first)
        trustedPlayers.sort(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(center)));
        
        return trustedPlayers;
    }
}