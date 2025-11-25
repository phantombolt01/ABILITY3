package com.oni.masks.abilities.impl;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.player.DamageUtils;
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

public class SpikeShieldAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final Map<UUID, Long> lastBarrierDamage = new HashMap<>();
    
    public SpikeShieldAbility(final Player player) {
        super("Spike Shield", player, 55);
    }
    
    @Override
    public void execute() {
        final Location playerLocation = this.player.getLocation();
        
        // Visual and audio effects
        this.plugin.getParticleManager().playEarthShieldAnimation(this.player);
        this.plugin.getSoundManager().playEarthShieldSound(this.player);
        
        // Create protective barrier effect (no physical blocks)
        new BukkitRunnable() {
            private int ticks = 0;
            private final int duration = 160; // 8 seconds
            private final double barrierRadius = 3.0;
            
            @Override
            public void run() {
                if (this.ticks >= duration) {
                    this.cancel();
                    return;
                }
                
                final Location currentLocation = player.getLocation();
                
                // Create barrier particles
                final int points = 16;
                for (int i = 0; i < points; i++) {
                    final double angle = (2 * Math.PI * i / points) + (this.ticks * 0.1);
                    final double x = barrierRadius * Math.cos(angle);
                    final double z = barrierRadius * Math.sin(angle);
                    
                    final Location particleLocation = currentLocation.clone().add(x, 1, z);
                    currentLocation.getWorld().spawnParticle(org.bukkit.Particle.BLOCK, particleLocation, 1, 
                            org.bukkit.Material.MOSSY_COBBLESTONE.createBlockData());
                    
                    // Add some crit particles for effect
                    if (this.ticks % 5 == 0) {
                        currentLocation.getWorld().spawnParticle(org.bukkit.Particle.CRIT, particleLocation, 1, 0.1, 0.1, 0.1, 0);
                    }
                }
                
                // Check for entities within barrier radius
                for (final Entity entity : currentLocation.getWorld().getNearbyEntities(currentLocation, barrierRadius, barrierRadius, barrierRadius)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        final LivingEntity livingEntity = (LivingEntity) entity;
                        
                        // Check if target can be harmed (trust system)
                        if (entity instanceof Player) {
                            final Player targetPlayer = (Player) entity;
                            if (!plugin.getTrustManager().canHarm(player, targetPlayer)) {
                                continue; // Skip trusted players - no knockback, no damage
                            }
                        }
                        
                        // Check if entity hasn't been damaged recently (prevent spam)
                        final UUID entityId = entity.getUniqueId();
                        final long currentTime = System.currentTimeMillis();
                        final Long lastDamageTime = lastBarrierDamage.get(entityId);
                        
                        if (lastDamageTime == null || currentTime - lastDamageTime > 1000) { // 1 second cooldown per entity
                            // Apply knockback
                            final Vector knockback = entity.getLocation().toVector()
                                    .subtract(currentLocation.toVector())
                                    .normalize()
                                    .multiply(1.5)
                                    .setY(0.3);
                            
                            livingEntity.setVelocity(knockback);
                            
                            DamageUtils.applyAdaptiveDamage(livingEntity, 1, player);
                            
                            
                            // Apply regen suppression
                            if (livingEntity instanceof Player) {
                                final Player targetPlayer = (Player) livingEntity;
                                targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, -40, -1)); // Remove regen
                                targetPlayer.sendMessage("§cRegen suppressed!");
                                
                                // Apply temporary regen lock
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (targetPlayer.isOnline()) {
                                            targetPlayer.sendMessage("§aRegen restored.Phantom Is the Goat so sub to him");
                                        }
                                    }
                                }.runTaskLater(plugin, 40L); // 2 seconds
                            }
                            
                            // Play impact effects
                            entity.getWorld().spawnParticle(org.bukkit.Particle.CRIT, entity.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.1);
                            entity.getWorld().playSound(entity.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 0.5f, 1.2f);
                            
                            // Record damage time
                            lastBarrierDamage.put(entityId, currentTime);
                        }
                    }
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L); // Every tick
        
        // Clean up damage tracking after barrier ends
        new BukkitRunnable() {
            @Override
            public void run() {
                lastBarrierDamage.clear();
            }
        }.runTaskLater(this.plugin, 180L); // Clean up after 9 seconds
        
        this.sendAbilityMessage("§a§l[Earth Mask] §7 A protective barrier surrounds you!");
    }
}