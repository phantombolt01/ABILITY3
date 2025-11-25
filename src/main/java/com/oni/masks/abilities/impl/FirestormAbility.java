package com.oni.masks.abilities.impl;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.player.DamageUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FirestormAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public FirestormAbility(final Player player) {
        super("Firestorm", player, 45);
    }
    
    @Override
    public void execute() {
        final Location playerLocation = this.player.getLocation();
        final Set<UUID> damagedEntities = new HashSet<>();
        
        // Rain fireballs from the sky
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 100) { // 5 seconds
                    this.cancel();
                    return;
                }
                
                // Spawn fireballs randomly in 15-block radius
                for (int i = 0; i < 3; i++) {
                    final double x = (Math.random() - 0.5) * 30; // 15 block radius
                    final double z = (Math.random() - 0.5) * 30;
                    final double y = 15 + Math.random() * 10; // High in the sky
                    
                    final Location fireballLocation = playerLocation.clone().add(x, y, z);
                    final Fireball fireball = fireballLocation.getWorld().spawn(fireballLocation, Fireball.class);
                    
                    fireball.setDirection(new org.bukkit.util.Vector(0, -1, 0)); // Fall straight down
                    fireball.setYield(1.5f); // Moderate explosion
                    fireball.setShooter(player);
                }
                
                // Fire particles raining down
                for (int i = 0; i < 20; i++) {
                    final double x = (Math.random() - 0.5) * 30;
                    final double z = (Math.random() - 0.5) * 30;
                    final double y = Math.random() * 5;
                    
                    final Location particleLocation = playerLocation.clone().add(x, y + 10, z);
                    playerLocation.getWorld().spawnParticle(org.bukkit.Particle.FLAME, 
                            particleLocation, 2, 0.2, 0.2, 0.2, 0.05);
                    playerLocation.getWorld().spawnParticle(org.bukkit.Particle.LAVA, 
                            particleLocation, 1, 0.1, 0.1, 0.1, 0);
                }
                
                // Damage entities in range
                for (final Entity entity : playerLocation.getWorld().getNearbyEntities(playerLocation, 15, 15, 15)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        final LivingEntity livingEntity = (LivingEntity) entity;
                        final UUID entityId = livingEntity.getUniqueId();
                        
                        // Prevent damage stacking
                        if (damagedEntities.contains(entityId)) {
                            continue;
                        }
                        
                        // Check trust system
                        if (entity instanceof Player) {
                            final Player targetPlayer = (Player) entity;
                            if (!plugin.getTrustManager().canHarm(player, targetPlayer)) {
                                continue;
                            }
                        }
                        
                        // Random chance to hit each tick
                        if (Math.random() < 0.1) { // 10% chance per tick
                            DamageUtils.applyAdaptiveDamage(livingEntity, 2.5, player);
                            livingEntity.setFireTicks(100); // 5 seconds of fire
                            damagedEntities.add(entityId);
                        }
                    }
                }
                
                // Play thunder sounds
                if (this.ticks % 30 == 0) {
                    player.getWorld().playSound(playerLocation, org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);
                    player.getWorld().playSound(playerLocation, org.bukkit.Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
        
        this.sendAbilityMessage("§c§l[WRATH] §7The infernal storm rains down upon your enemies!");
    }
}