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

public class WaterDashAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PluginConfig config = this.plugin.getConfigManager().getPluginConfig();
    
    public WaterDashAbility(final Player player) {
        super("Water Dash", player, 30);
    }
    
    @Override
    public void execute() {
        final Location startLocation = this.player.getLocation();
        final Vector direction = this.player.getEyeLocation().getDirection();
        direction.setY(0.2); // Slight upward trajectory
        direction.normalize();
        
        // Visual and audio effects
        this.plugin.getParticleManager().playWaterWaveAnimation(this.player);
        this.plugin.getSoundManager().playWaterWaveSound(this.player);
        
        // Give temporary speed boost and damage on contact
        this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2)); // 2 seconds
        this.player.setVelocity(direction.multiply(2.0));
        
        // Create water trail and damage entities
        new BukkitRunnable() {
            private int ticks = 0;
            private final Map<UUID, Boolean> damagedEntities = new HashMap<>();
            
            @Override
            public void run() {
                if (this.ticks >= 40) { // 2 seconds
                    this.cancel();
                    return;
                }
                
                final Location currentLocation = player.getLocation();
                
                // Create water trail particles
                plugin.getParticleManager().playWaterWaveParticles(currentLocation);
                
                // Damage nearby entities during dash
                for (final Entity entity : currentLocation.getWorld().getNearbyEntities(currentLocation, 2, 2, 2)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        final LivingEntity livingEntity = (LivingEntity) entity;
                        
                        // Prevent damage stacking - only damage once per cast
                        final UUID entityId = livingEntity.getUniqueId();
                        if (this.damagedEntities.containsKey(entityId)) {
                            continue; // Already damaged by this cast
                        }
                        
                        // Check if target can be harmed
                        if (entity instanceof Player) {
                            final Player targetPlayer = (Player) entity;
                            if (!plugin.getTrustManager().canHarm(player, targetPlayer)) {
                                continue;
                            }
                        }
                        
                        DamageUtils.applyAdaptiveDamage(livingEntity, 1.5, player);
                        this.damagedEntities.put(entityId, true);
                        final Vector knockback = direction.clone().multiply(0.8);
                        livingEntity.setVelocity(knockback);
                    }
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
        
        this.sendAbilityMessage("§b§l[Water Mask] §7You surge forward with the power of the tide!");
    }
}