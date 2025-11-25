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

import java.util.ArrayList;
import java.util.List;

public class StormCallAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public StormCallAbility(final Player player) {
        super("Storm Call", player, 90);
    }
    
    @Override
    public void execute() {
        final Location playerLocation = this.player.getLocation();
        
        // Visual and audio effects
        this.plugin.getParticleManager().playStormCall(this.player);
        this.plugin.getSoundManager().playStormCallSound(this.player);
        
        // Find all valid targets instantly
        final List<LivingEntity> targets = new ArrayList<>();
        for (final Entity entity : playerLocation.getWorld().getNearbyEntities(playerLocation, 12, 12, 12)) {
            if (entity instanceof LivingEntity && !entity.equals(this.player)) {
                final LivingEntity livingEntity = (LivingEntity) entity;
                
                // Check if target can be harmed (trust system)
                if (entity instanceof Player) {
                    final Player targetPlayer = (Player) entity;
                    if (!this.plugin.getTrustManager().canHarm(this.player, targetPlayer)) {
                        continue; // Skip trusted players
                    }
                }
                
                targets.add(livingEntity);
            }
        }
        
        // Strike each target with multiple lightning bolts instantly
        new BukkitRunnable() {
            private int strikes = 0;
            private final int maxStrikes = targets.size() * 3; // 3 strikes per target
            
            @Override
            public void run() {
                if (this.strikes >= maxStrikes || targets.isEmpty()) {
                    this.cancel();
                    return;
                }
                
                // Strike up to 3 targets per tick for smooth effect
                for (int i = 0; i < Math.min(3, targets.size()); i++) {
                    final LivingEntity target = targets.get((int) (Math.random() * targets.size()));
                    
                    // Strike with lightning instantly
                    target.getWorld().strikeLightningEffect(target.getLocation());
                    
                    DamageUtils.applyAdaptiveDamage(target, 2, player);

                        target.damage(0.1, player);
                        target.setFireTicks(60); // 3 seconds of fire
                    
                    if (target instanceof Player) {
                        final Player targetPlayer = (Player) target;
                        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1)); // 2 seconds Slowness II
                    }
                    
                    // Lightning particles
                    plugin.getParticleManager().playLightningStrike(target.getLocation());
                    
                    this.strikes++;
                }
            }
        }.runTaskTimer(this.plugin, 0L, 2L); // Start immediately, strike every 2 ticks
        
        // Send ability message
        final Component message = Component.text()
                .append(Component.text("⚡ ", NamedTextColor.YELLOW))
                .append(Component.text("§9§l[Lightning Mask] §7 You call down the storm. Lightning punishes all.", NamedTextColor.GOLD))
                .build();
        
        this.player.sendMessage(message);
    }
    
    @Override
    protected int getCooldownMs() {
        return 40000; // 35 seconds
    }
}