package com.oni.masks.abilities.impl;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.player.DamageUtils;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class VoidEruptionAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public VoidEruptionAbility(final Player player) {
        super("Void Eruption", player, 60);
    }
    
    @Override
    public void execute() {
        final Location playerLocation = this.player.getLocation();
        
        // Visual and audio effects
        this.plugin.getParticleManager().playVoidEruption(this.player);
        this.plugin.getSoundManager().playVoidEruptionSound(this.player);
        
        // Create ring particles around player (final spec)
        this.createVoidRing(playerLocation);
        
        // Create explosive void effect - instant burst
        new BukkitRunnable() {
            @Override
            public void run() {
                // Find all enemies in range and blast them away
                for (final Entity entity : playerLocation.getWorld().getNearbyEntities(playerLocation, 12, 12, 12)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        final LivingEntity livingEntity = (LivingEntity) entity;
                        
                        // Check if target can be harmed (trust system)
                        if (entity instanceof Player) {
                            final Player targetPlayer = (Player) entity;
                            if (!plugin.getTrustManager().canHarm(player, targetPlayer)) {
                                continue; // Skip trusted players
                            }
                        }
                        
                        // Violent knockback away from player
                        final Vector knockback = entity.getLocation().toVector()
                                .subtract(playerLocation.toVector())
                                .normalize()
                                .multiply(3.0) // Strong knockback
                                .setY(1.0); // Strong upward component
                        
                        entity.setVelocity(knockback);
                        
                        DamageUtils.applyAdaptiveDamage(livingEntity, 2, player);
                        
                        // Apply disorientation
                        if (livingEntity instanceof Player) {
                            final Player targetPlayer = (Player) livingEntity;
                            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0)); // 2 seconds
                            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 90, 1)); // 3 seconds
                        }
                        
                        // Void explosion particles on target
                        plugin.getParticleManager().playVoidExplosionEffect(entity.getLocation());
                    }
                }
            }
        }.runTask(this.plugin); // Execute immediately
        
        // Send ability message
        final Component message = Component.text()
                .append(Component.text("☠ ", NamedTextColor.DARK_GRAY))
                .append(Component.text("§5§l[Void Mask] §7 You erupt with void energy. Darkness rejects all around you.", NamedTextColor.GRAY))
                .build();
        
        this.player.sendMessage(message);
    }
    
    private void createVoidRing(final Location center) {
        // Create ring particles around player (final visual spec)
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 20) { // 1 second ring effect
                    this.cancel();
                    return;
                }
                
                final double radius = 3.0 + (this.ticks * 0.2);
                for (int i = 0; i < 16; i++) {
                    final double angle = (2 * Math.PI * i / 16);
                    final double x = radius * Math.cos(angle);
                    final double z = radius * Math.sin(angle);
                    
                    final Location ringLocation = center.clone().add(x, 0.5, z);
                    center.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH, 
                            ringLocation, 2, 0.1, 0.1, 0.1, 0.02);
                    center.getWorld().spawnParticle(org.bukkit.Particle.SOUL, 
                            ringLocation, 1, 0.1, 0.1, 0.1, 0.01);
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    @Override
    protected int getCooldownMs() {
        return 50000; // 35 seconds
    }
}