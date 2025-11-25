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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PrimordialFlameAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PluginConfig config = this.plugin.getConfigManager().getPluginConfig();
    private final Map<UUID, Boolean> damagedEntities = new HashMap<>();
    
    public PrimordialFlameAbility(final Player player) {
        super("Primordial Flame", player, 65);
    }
    
    @Override
    public void execute() {
        final Location playerLocation = this.player.getLocation();
        
        // Visual and audio effects
        this.plugin.getParticleManager().playPrimordialFlameOrb(this.player);
        this.plugin.getSoundManager().playPrimordialFlameOrb(this.player);
        
        // Find all targets in 15-block radius (final spec)
        final List<LivingEntity> targets = this.findTargetsInCircle(playerLocation, 15.0);
        final int maxTargets = Math.min(targets.size(), this.config.getMaxTargetsPerAbility());
        
        // Create expanding flame orb effect
        new BukkitRunnable() {
            private int ticks = 0;
            private final double maxRadius = 15.0; // Final spec: 15-block radius
            
            @Override
            public void run() {
                if (this.ticks >= 30) { // 1.5 seconds duration
                    this.cancel();
                    // Clear damage tracking when ability ends
                    damagedEntities.clear();
                    return;
                }
                
                final double currentRadius = (this.ticks / 30.0) * maxRadius;
                
                // Create circular flame wave
                for (double angle = 0; angle < 2 * Math.PI; angle += 0.2) {
                    for (double radius = 1; radius <= currentRadius; radius += 0.8) {
                        final double x = radius * Math.cos(angle);
                        final double z = radius * Math.sin(angle);
                        
                        final Location flameLocation = playerLocation.clone().add(x, 0.5, z);
                        
                        // Premium flame particles (final visual overhaul)
                        flameLocation.getWorld().spawnParticle(org.bukkit.Particle.FLAME, 
                                flameLocation, 5, 0.3, 0.3, 0.3, 0.08);
                        flameLocation.getWorld().spawnParticle(org.bukkit.Particle.LAVA, 
                                flameLocation, 2, 0.2, 0.2, 0.2, 0);
                        flameLocation.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, 
                                flameLocation, 1, 0.1, 0.1, 0.1, 0.02);
                        
                        // Damage entities in flame wave
                        PrimordialFlameAbility.this.damageEntitiesInFlame(flameLocation);
                    }
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
        
        final Component message = Component.text()
                .append(Component.text("🔥 ", NamedTextColor.GOLD))
                .append(Component.text("§6§l[Primordial Flame] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("§7 The flames of creation consume all!", NamedTextColor.YELLOW))
                .build();
        
        this.player.sendMessage(message);
    }
    
    private List<LivingEntity> findTargetsInCircle(final Location center, final double radius) {
        final List<LivingEntity> targets = new ArrayList<>();
        
        for (final Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(this.player)) {
                final LivingEntity livingEntity = (LivingEntity) entity;
                
                // Check if target can be harmed (trust system)
                if (entity instanceof Player) {
                    final Player targetPlayer = (Player) entity;
                    if (!this.plugin.getTrustManager().canHarm(this.player, targetPlayer)) {
                        continue; // Skip trusted players
                    }
                }
                
                // Check if within circular range
                if (center.distance(entity.getLocation()) <= radius) {
                    targets.add(livingEntity);
                }
            }
        }
        
        return targets;
    }
    
    private void damageEntitiesInFlame(final Location flameLocation) {
        for (final Entity entity : flameLocation.getWorld().getNearbyEntities(flameLocation, 1.5, 2, 1.5)) {
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
                
                DamageUtils.applyAdaptiveDamage(livingEntity, 3.5, player);
                damagedEntities.put(entityId, true);

                
                // Set on fire for 5 seconds
                livingEntity.setFireTicks(100);
                
                // Apply knockback
                final Vector knockback = entity.getLocation().toVector()
                        .subtract(this.player.getLocation().toVector())
                        .normalize()
                        .multiply(2.0)
                        .setY(0.5);
                livingEntity.setVelocity(knockback);
                
                // Apply regen suppression
                if (livingEntity instanceof Player) {
                    final Player targetPlayer = (Player) livingEntity;
                    targetPlayer.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.REGENERATION, -60, -1));
                    targetPlayer.sendMessage("§cRegen suppressed by primordial flames!");
                    
                    // Restore regen after 3 seconds
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (targetPlayer.isOnline()) {
                                targetPlayer.sendMessage("§aRegen restored.");
                            }
                        }
                    }.runTaskLater(this.plugin, 60L);
                }
                
                // Intense impact effects
                livingEntity.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, 
                        livingEntity.getLocation(), 1, 0, 0, 0, 0);
                livingEntity.getWorld().spawnParticle(org.bukkit.Particle.LAVA, 
                        livingEntity.getLocation().add(0, 1, 0), 8, 0.5, 0.5, 0.5, 0.1);
                livingEntity.getWorld().playSound(livingEntity.getLocation(), 
                        org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.2f);
            }
        }
    }
    
    @Override
    protected int getCooldownMs() {
        return 65000; // 8 seconds cooldown
    }
}