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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class LightOrbVolleyAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PluginConfig config = this.plugin.getConfigManager().getPluginConfig();
    private final Map<UUID, Boolean> damagedEntities = new HashMap<>();
    
    public LightOrbVolleyAbility(final Player player) {
        super("Light Orb Volley", player, 50);
    }
    
    @Override
    public void execute() {
        final Location eyeLocation = this.player.getEyeLocation();
        final Vector direction = eyeLocation.getDirection();
        
        // Visual and audio effects
        this.plugin.getParticleManager().playLightBallAnimation(this.player);
        this.plugin.getSoundManager().playLightBallSound(this.player);
        
        // Create light beam attack instead of projectiles
        new BukkitRunnable() {
            private int ticks = 0;
            private final double maxRange = 15.0;
            
            @Override
            public void run() {
                if (this.ticks >= 20) { // 1 second duration
                    this.cancel();
                    // Clear damage tracking when ability ends
                    damagedEntities.clear();
                    return;
                }
                
                // Create light beam particles
                for (double distance = 1; distance <= maxRange; distance += 0.5) {
                    final Location beamLocation = eyeLocation.clone()
                            .add(direction.clone().multiply(distance));
                    
                    // Light beam particles
                    beamLocation.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, 
                            beamLocation, 2, 0.1, 0.1, 0.1, 0.02);
                    beamLocation.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, 
                            beamLocation, 1, 0.1, 0.1, 0.1, 0);
                    
                    // Check for entities to damage
                    damageEntitiesInBeam(beamLocation);
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L); // Every tick
        
        this.sendAbilityMessage("§e§l[Light Mask] §7 Radiant beams pierce through darkness!");
    }
    
    private void damageEntitiesInBeam(final Location beamLocation) {
        for (final Entity entity : beamLocation.getWorld().getNearbyEntities(beamLocation, 1, 1, 1)) {
            if (entity instanceof LivingEntity && !entity.equals(this.player)) {
                final LivingEntity livingEntity = (LivingEntity) entity;
                
                // Prevent damage stacking - only damage once per cast
                final UUID entityId = livingEntity.getUniqueId();
                if (damagedEntities.containsKey(entityId)) {
                    continue; // Already damaged by this cast
                }
                
                // Check if target can be harmed
                if (entity instanceof Player) {
                    final Player targetPlayer = (Player) entity;
                    if (!this.plugin.getTrustManager().canHarm(this.player, targetPlayer)) {
                        continue;
                    }
                }
                
                DamageUtils.applyAdaptiveDamage(livingEntity, 1, player);
                damagedEntities.put(entityId, true);

                
                // Light impact effects
                livingEntity.getWorld().spawnParticle(org.bukkit.Particle.FLASH, 
                        livingEntity.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
                livingEntity.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, 
                        livingEntity.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
                        livingEntity.getWorld().spawnParticle(org.bukkit.Particle.FLASH, 
                        livingEntity.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
                livingEntity.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, 
                        livingEntity.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
                
                // Play glass chime sound
                livingEntity.getWorld().playSound(livingEntity.getLocation(), 
                        org.bukkit.Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
            }
        }
    }
}