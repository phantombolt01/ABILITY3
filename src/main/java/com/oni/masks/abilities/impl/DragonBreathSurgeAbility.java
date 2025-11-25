package com.oni.masks.abilities.impl;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.player.DamageUtils;
import org.bukkit.Location;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DragonBreathSurgeAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public DragonBreathSurgeAbility(final Player player) {
        super("Dragon's Breath Surge", player, 35);
    }
    
    @Override
    public void execute() {
        final Location eyeLocation = this.player.getEyeLocation();
        final Vector direction = eyeLocation.getDirection();
        
        // Create dragon breath cone effect
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 60) { // 3 seconds
                    this.cancel();
                    return;
                }
                
                // Create cone-shaped dragon breath
                final double coneAngle = Math.PI / 6; // 30 degrees
                final double maxRange = 12.0;
                
                for (double angle = -coneAngle / 2; angle <= coneAngle / 2; angle += 0.1) {
                    for (double distance = 1; distance <= maxRange; distance += 0.5) {
                        final Vector coneDirection = direction.clone();
                        coneDirection.rotateAroundY(angle);
                        
                        final Location breathLocation = eyeLocation.clone()
                                .add(coneDirection.multiply(distance));
                        
                        // Dragon breath particles
                        breathLocation.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH, 
                                breathLocation, 3, 0.3, 0.3, 0.3, 0.02);
                        breathLocation.getWorld().spawnParticle(org.bukkit.Particle.WITCH, 
                                breathLocation, 1, 0.2, 0.2, 0.2, 0.01);
                        
                        // Create lingering area effect cloud
                        if (this.ticks % 10 == 0 && distance <= 8) {
                            final AreaEffectCloud cloud = breathLocation.getWorld().spawn(breathLocation, AreaEffectCloud.class);
                            cloud.setDuration(100); // 5 seconds
                            cloud.setRadius(2.0f);
                            cloud.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 0), true);
                            cloud.setSource(player);
                        }
                        
                        // Damage entities in breath
                        for (final Entity entity : breathLocation.getWorld().getNearbyEntities(breathLocation, 1.5, 1.5, 1.5)) {
                            if (entity instanceof LivingEntity && !entity.equals(player)) {
                                final LivingEntity livingEntity = (LivingEntity) entity;
                                
                                // Check trust system
                                if (entity instanceof Player) {
                                    final Player targetPlayer = (Player) entity;
                                    if (!plugin.getTrustManager().canHarm(player, targetPlayer)) {
                                        continue;
                                    }
                                }
                                
                                // Apply damage and burning
                                DamageUtils.applyAdaptiveDamage(livingEntity , distance, player);
                                livingEntity.setFireTicks(40); // 2 seconds of fire
                            }
                        }
                    }
                }
                
                // Play sounds periodically
                if (this.ticks % 20 == 0) {
                    player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
                    player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.2f);
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
        
        this.sendAbilityMessage("§5§l[PRIDE] §7Dragon's breath surges forth in royal fury!");
    }
}