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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WaterWaveAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PluginConfig config = this.plugin.getConfigManager().getPluginConfig();
    
    public WaterWaveAbility(final Player player) {
        super("Water Wave", player, 40);
    }
    
    @Override
    public void execute() {
        final Location startLocation = this.player.getLocation();
        final Vector direction = this.player.getEyeLocation().getDirection();
        direction.setY(0); // Keep wave on ground level
        direction.normalize();
        
        // Visual and audio effects
        this.plugin.getParticleManager().playWaterWaveAnimation(this.player);
        this.plugin.getSoundManager().playWaterWaveSound(this.player);
        
        // Create expanding tidal wave in cone shape
        new BukkitRunnable() {
            private int ticks = 0;
            private final double maxRange = 10.0;
            private final double coneAngle = Math.PI / 3; // 60 degrees
            private final Map<UUID, Boolean> damagedEntities = new HashMap<>();
            
            @Override
            public void run() {
                if (this.ticks >= 20) { // 1 second duration
                    this.cancel();
                    return;
                }
                
                final double currentRange = (this.ticks / 20.0) * maxRange;
                
                // Create cone-shaped wave
                for (double angle = -coneAngle / 2; angle <= coneAngle / 2; angle += 0.1) {
                    for (double distance = 1; distance <= currentRange; distance += 0.5) {
                        final Vector waveDirection = direction.clone();
                        waveDirection.rotateAroundY(angle);
                        
                        final Location waveLocation = startLocation.clone()
                                .add(waveDirection.multiply(distance));
                        
                        // Create wave particles
                        plugin.getParticleManager().playWaterWaveParticles(waveLocation);
                        
                        // Check for entities to damage
                        WaterWaveAbility.this.damageEntitiesInWave(waveLocation, this.damagedEntities);
                    }
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L); // Every tick for smooth wave
        
        this.sendAbilityMessage("§b§l[Water Mask] §7 A mighty tidal wave crashes forward!");
    }
    
    private void damageEntitiesInWave(final Location waveLocation, final Map<UUID, Boolean> damagedEntities) {
        for (final Entity entity : waveLocation.getWorld().getNearbyEntities(waveLocation, 1.5, 2, 1.5)) {
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
                        continue; // Skip trusted players
                    }
                }
                
                DamageUtils.applyAdaptiveDamage(livingEntity, 3, player);
                damagedEntities.put(entityId, true);
                
                // Apply Knockback II effect
                final Vector knockback = waveLocation.toVector()
                        .subtract(this.player.getLocation().toVector())
                        .normalize()
                        .multiply(2.0)
                        .setY(0.5);
                livingEntity.setVelocity(knockback);
                
                // Apply brief slowness
                if (livingEntity instanceof Player) {
                    final Player targetPlayer = (Player) livingEntity;
                    targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1)); // 2 seconds
                }
                
                // Play splash sound and particles on hit
                livingEntity.getWorld().playSound(livingEntity.getLocation(), 
                        org.bukkit.Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.0f);
                livingEntity.getWorld().spawnParticle(org.bukkit.Particle.SPLASH, 
                        livingEntity.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }
}