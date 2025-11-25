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

public class FlameBlastAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PluginConfig config = this.plugin.getConfigManager().getPluginConfig();
    
    public FlameBlastAbility(final Player player) {
        super("Flame Blast", player, 50);
    }
    
    @Override
    public void execute() {
        final Location eyeLocation = this.player.getEyeLocation();
        final Vector direction = eyeLocation.getDirection();
        
        // Visual and audio effects
        this.plugin.getParticleManager().playFlameAnimation(this.player);
        this.plugin.getSoundManager().playFireballSound(this.player);
        
        // Create cone blast effect
        new BukkitRunnable() {
            private int ticks = 0;
            private final Map<UUID, Boolean> damagedEntities = new HashMap<>();
            
            @Override
            public void run() {
                if (ticks >= 20) {
                    cancel();
                    return;
                }
                
                final double range = 8.0;
                final double coneAngle = Math.PI / 6; // 30 degrees
                
                for (int i = 0; i < 5; i++) {
                    final double angle = (Math.random() - 0.5) * coneAngle;
                    final Vector coneDirection = direction.clone();
                    
                    // Rotate vector for cone spread
                    final double cos = Math.cos(angle);
                    final double sin = Math.sin(angle);
                    final double x = coneDirection.getX() * cos - coneDirection.getZ() * sin;
                    final double z = coneDirection.getX() * sin + coneDirection.getZ() * cos;
                    coneDirection.setX(x);
                    coneDirection.setZ(z);
                    
                    final Location fireLocation = eyeLocation.clone()
                            .add(coneDirection.multiply(ticks * 0.5));
                    
                    // Spawn fire particles
                    fireLocation.getWorld().spawnParticle(
                            org.bukkit.Particle.FLAME, fireLocation, 3, 0.2, 0.2, 0.2, 0.02
                    );
                    
                    // Damage entities in cone
                    for (final Entity entity : fireLocation.getWorld().getNearbyEntities(fireLocation, 1.5, 1.5, 1.5)) {
                        if (entity instanceof LivingEntity && !entity.equals(player)) {
                            final LivingEntity livingEntity = (LivingEntity) entity;
                            final UUID entityId = livingEntity.getUniqueId();
                            
                            // Prevent damage stacking
                            if (damagedEntities.containsKey(entityId)) {
                                continue;
                            }
                            
                     DamageUtils.applyAdaptiveDamage(livingEntity, 2, player);
                            livingEntity.setFireTicks(60); // 3s fire
                            damagedEntities.put(entityId, true);
                        }
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
        
        this.sendAbilityMessage("§c§l[Flame Mask] §7Cone of flames erupts forward!");
    }
}
