package com.oni.masks.abilities.impl;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class WeightOfTheWorldAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public WeightOfTheWorldAbility(final Player player) {
        super("Weight of the World", player, 50);
    }
    
    @Override
    public void execute() {
        final Location playerLocation = this.player.getLocation();
        
        // Create persistent slow field for 10 seconds
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 200) { // 10 seconds
                    this.cancel();
                    return;
                }
                
                // Apply slowness to all players in 8-block radius
                for (final Entity entity : playerLocation.getWorld().getNearbyEntities(playerLocation, 8, 8, 8)) {
                    if (entity instanceof Player && !entity.equals(player)) {
                        final Player targetPlayer = (Player) entity;
                        
                        // Check trust system
                        if (!plugin.getTrustManager().canHarm(player, targetPlayer)) {
                            continue;
                        }
                        
                        // Apply Slowness II
                        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                    }
                }
                
                // Gray-blue pulsing rings
                if (this.ticks % 20 == 0) { // Every second
                    for (int ring = 1; ring <= 3; ring++) {
                        final double radius = ring * 2.5;
                        for (int i = 0; i < 16; i++) {
                            final double angle = (2 * Math.PI * i / 16);
                            final double x = radius * Math.cos(angle);
                            final double z = radius * Math.sin(angle);
                            
                            final Location ringLocation = playerLocation.clone().add(x, 0.5, z);
                            playerLocation.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, 
                                    ringLocation, 1, 0.1, 0.1, 0.1, 0.02);
                            playerLocation.getWorld().spawnParticle(org.bukkit.Particle.ASH, 
                                    ringLocation, 1, 0.1, 0.1, 0.1, 0.01);
                        }
                    }
                }
                
                // Play heartbeat sound every 2 seconds
                if (this.ticks % 40 == 0) {
                    player.getWorld().playSound(playerLocation, org.bukkit.Sound.ENTITY_WARDEN_HEARTBEAT, 0.8f, 0.5f);
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
        
        this.sendAbilityMessage("§7§l[SLOTH] §7The weight of eternity burdens all around you!");
    }
}