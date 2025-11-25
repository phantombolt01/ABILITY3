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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class LightningBurstAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public LightningBurstAbility(final Player player) {
        super("Lightning Burst", player,60 );
    }
    
    @Override
    public void execute() {
        final Location playerLocation = this.player.getLocation();
        
        // Visual and audio effects
        this.plugin.getParticleManager().playLightningBurst(this.player);
        this.plugin.getSoundManager().playLightningBurstSound(this.player);
        
        // Strike self with dramatic lightning (no damage)
        new BukkitRunnable() {
            private int strikes = 0;
            
            @Override
            public void run() {
                if (this.strikes >= 3) {
                    this.cancel();
                    return;
                }
                
                // Strike player with visual lightning
                playerLocation.getWorld().strikeLightningEffect(playerLocation);
                plugin.getParticleManager().playLightningExplosion(playerLocation);
                
                this.strikes++;
            }
        }.runTaskTimer(this.plugin, 0L, 3L); // 3 strikes, 0.15s apart
        
        // Radial lightning explosion - execute instantly
        new BukkitRunnable() {
            @Override
            public void run() {
                // Find all enemies in 12-block radius
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
                        
                        // Radial knockback with 12-block boundary enforcement
                        final Vector knockback = entity.getLocation().toVector()
                                .subtract(playerLocation.toVector())
                                .normalize()
                                .multiply(2.5)
                                .setY(0.8); // Strong knockback with upward component
                        
                        entity.setVelocity(knockback);
                    DamageUtils.applyAdaptiveDamage(livingEntity, 2.5, player);
                        
                        // Lightning strike on each target
                        entity.getWorld().strikeLightningEffect(entity.getLocation());
                        plugin.getParticleManager().playLightningImpact(entity.getLocation());
                    }
                }
            }
        }.runTaskLater(this.plugin, 10L); // Execute after self-strikes
        
        // Send ability message
        final Component message = Component.text()
                .append(Component.text("⚡ ", NamedTextColor.YELLOW))
                .append(Component.text("§9§l[Lightning Mask] §7You erupt in lightning. The storm explodes from within you.", NamedTextColor.GOLD))
                .build();
        
        this.player.sendMessage(message);
    }
    
    @Override
    protected int getCooldownMs() {
        return 60000; // 25 seconds
    }
}