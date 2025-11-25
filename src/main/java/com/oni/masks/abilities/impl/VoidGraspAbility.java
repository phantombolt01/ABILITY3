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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoidGraspAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public VoidGraspAbility(final Player player) {
        super("Void Grasp", player, 40);
    }
    
    @Override
    public void execute() {
        final Location playerLocation = this.player.getLocation();
        
        // Visual and audio effects
        this.plugin.getParticleManager().playVoidGrasp(this.player);
        this.plugin.getSoundManager().playVoidGraspSound(this.player);
        
        // Create spiraling purple vortex effect (final spec)
        this.createVoidVortex(playerLocation);
        
        // Create void rift effect - persistent pull over 1 second
        new BukkitRunnable() {
            private int ticks = 0;
            private final Map<UUID, Boolean> damagedEntities = new HashMap<>();
            
            @Override
            public void run() {
                if (this.ticks >= 20) { // 1 second duration
                    this.cancel();
                    return;
                }
                
                // Find enemies within range and pull them in
                for (final Entity entity : playerLocation.getWorld().getNearbyEntities(playerLocation, 8, 8, 8)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        final LivingEntity livingEntity = (LivingEntity) entity;
                        
                        // Prevent damage stacking - only damage once per cast
                        final UUID entityId = livingEntity.getUniqueId();
                        if (this.damagedEntities.containsKey(entityId)) {
                            continue; // Already damaged by this cast
                        }
                        
                        // Check if target can be harmed (trust system)
                        if (entity instanceof Player) {
                            final Player targetPlayer = (Player) entity;
                            if (!plugin.getTrustManager().canHarm(player, targetPlayer)) {
                                continue; // Skip trusted players
                            }
                        }
                        
                        // Pull entity toward player (persistent drag)
                        final Vector pullDirection = playerLocation.toVector()
                                .subtract(entity.getLocation().toVector())
                                .normalize()
                                .multiply(0.4) // Stronger pull than before
                                .setY(0.1); // Slight upward lift
                        
                        entity.setVelocity(entity.getVelocity().add(pullDirection));
                        
                        DamageUtils.applyAdaptiveDamage(livingEntity, 3, player);
                        this.damagedEntities.put(entityId, true);

                            
                            // Apply debuffs
                            if (livingEntity instanceof Player) {
                                final Player targetPlayer = (Player) livingEntity;
                                targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1)); // 2 seconds Slowness II
                                targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 90, 0)); // 1 second Wither
                            }
                            
                            // Void particles on target
                            plugin.getParticleManager().playVoidDamageEffect(entity.getLocation());
                    }
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
        
        // Send ability message
        final Component message = Component.text()
                .append(Component.text("☠ ", NamedTextColor.DARK_GRAY))
                .append(Component.text("§5§l[Void Mask] §7 You unleash the Void's grasp. The abyss pulls all into silence.", NamedTextColor.GRAY))
                .build();
        
        this.player.sendMessage(message);
    }
    
    private void createVoidVortex(final Location center) {
        // Create spiraling purple vortex effect (final visual spec)
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 20) { // 1 second vortex effect
                    this.cancel();
                    return;
                }
                
                final double radius = 4.0 - (this.ticks * 0.1); // Shrinking spiral
                for (int i = 0; i < 12; i++) {
                    final double angle = (2 * Math.PI * i / 12) + (this.ticks * 0.3); // Spiral motion
                    final double x = radius * Math.cos(angle);
                    final double z = radius * Math.sin(angle);
                    final double y = Math.sin(this.ticks * 0.2) * 1.5; // Vertical wave
                    
                    final Location vortexLocation = center.clone().add(x, y, z);
                    center.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH, 
                            vortexLocation, 3, 0.1, 0.1, 0.1, 0.02);
                    center.getWorld().spawnParticle(org.bukkit.Particle.WITCH, 
                            vortexLocation, 1, 0.1, 0.1, 0.1, 0.01);
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    @Override
    protected int getCooldownMs() {
        return 55000; // 25 seconds
    }
}