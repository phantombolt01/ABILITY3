package com.oni.masks.abilities.impl;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.config.PluginConfig;
import com.oni.masks.player.DamageUtils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class LightBallAbility extends Ability implements Listener {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PluginConfig config = this.plugin.getConfigManager().getPluginConfig();
    
    public LightBallAbility(final Player player) {
        super("Light Ball", player, 50);
    }
    
    @Override
    public void execute() {
        final Location eyeLocation = this.player.getEyeLocation();
        final Vector direction = eyeLocation.getDirection();
        
        // Play light ball animation
        this.plugin.getParticleManager().playLightBallAnimation(this.player);
        this.plugin.getSoundManager().playLightBallSound(this.player);
        
        // Launch light balls (using snowballs as projectiles)
        for (int i = 0; i < 3; i++) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    final Snowball lightBall = player.launchProjectile(Snowball.class);
                    lightBall.setVelocity(direction.clone().multiply(2));
                    
                    // Add glowing particles trail
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (lightBall.isDead()) {
                                this.cancel();
                                return;
                            }
                            
                            plugin.getParticleManager().playLightBallTrail(lightBall.getLocation());
                        }
                    }.runTaskTimer(plugin, 0L, 1L);
                }
            }.runTaskLater(this.plugin, i * 5L); // Stagger the shots
        }
        
        this.sendAbilityMessage("§e§l[Light Mask] §7 Orbs of light streak toward your enemies!");
    }
    
    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball)) {
            return;
        }
        
        final Snowball snowball = (Snowball) event.getEntity();
        if (!(snowball.getShooter() instanceof Player)) {
            return;
        }
        
        final Player shooter = (Player) snowball.getShooter();
        // Check if this is a light ball from a Light Mask user
        // (In a real implementation, you'd want to tag the snowball somehow)
        
        final Location hitLocation = snowball.getLocation();
        
        // Create light explosion effect
        this.plugin.getParticleManager().playLightExplosion(hitLocation);
        this.plugin.getSoundManager().playLightImpactSound(hitLocation);
        
        // Damage nearby enemies
        for (final Entity entity : hitLocation.getWorld().getNearbyEntities(hitLocation, 2, 2, 2)) {
            if (entity instanceof LivingEntity && !entity.equals(shooter)) {
                final LivingEntity livingEntity = (LivingEntity) entity;
                
                // Check if target can be harmed
                if (entity instanceof Player) {
                    final Player targetPlayer = (Player) entity;
                    if (!this.plugin.getTrustManager().canHarm(shooter, targetPlayer)) {
                        continue; // Skip trusted players
                    }
                }
                
                
                DamageUtils.applyAdaptiveDamage(livingEntity, 1, shooter);
            }
        }
    }
}