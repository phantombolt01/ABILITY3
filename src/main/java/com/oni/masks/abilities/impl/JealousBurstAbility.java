package com.oni.masks.abilities.impl;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.player.DamageUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class JealousBurstAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public JealousBurstAbility(final Player player) {
        super("Jealous Burst", player, 40);
    }
    
    @Override
    public void execute() {
        final Location playerLocation = this.player.getLocation();
        final List<LivingEntity> targets = new ArrayList<>();
        
        // Find all valid targets in 6-block radius
        for (final Entity entity : playerLocation.getWorld().getNearbyEntities(playerLocation, 6, 6, 6)) {
            if (entity instanceof LivingEntity && !entity.equals(this.player)) {
                final LivingEntity livingEntity = (LivingEntity) entity;
                
                // Check trust system
                if (entity instanceof Player) {
                    final Player targetPlayer = (Player) entity;
                    if (!this.plugin.getTrustManager().canHarm(this.player, targetPlayer)) {
                        continue;
                    }
                }
                
                targets.add(livingEntity);
            }
        }
        
        // Apply effects to all targets
        for (final LivingEntity target : targets) {
            // Deal 2 hearts true damage
            DamageUtils.applyAdaptiveDamage(target, 2, player);
            
            // Steal a random potion effect
            final List<PotionEffect> activeEffects = new ArrayList<>(target.getActivePotionEffects());
            if (!activeEffects.isEmpty()) {
                final PotionEffect stolenEffect = activeEffects.get((int) (Math.random() * activeEffects.size()));
                target.removePotionEffect(stolenEffect.getType());
                this.player.addPotionEffect(new PotionEffect(stolenEffect.getType(), 
                        stolenEffect.getDuration(), stolenEffect.getAmplifier()));
            }
            
            // Green toxic particles on target
            target.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, 
                    target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
            target.getWorld().spawnParticle(org.bukkit.Particle.WITCH, 
                    target.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.05);
        }
        
        // Heal player for each target hit
        final double healAmount = targets.size() * 2.0; // 1 heart per target
        final double newHealth = Math.min(this.player.getMaxHealth(), this.player.getHealth() + healAmount);
        this.player.setHealth(newHealth);
        
        // Green burst particles around player
        playerLocation.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, 
                playerLocation.add(0, 1, 0), 20, 2, 2, 2, 0.2);
        playerLocation.getWorld().spawnParticle(org.bukkit.Particle.WITCH, 
                playerLocation, 15, 1.5, 1.5, 1.5, 0.1);
        
        // Play envious sounds
        this.player.getWorld().playSound(playerLocation, org.bukkit.Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 0.8f);
        this.player.getWorld().playSound(playerLocation, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        
        this.sendAbilityMessage("§a§l[ENVY] §7The green hunger devours " + targets.size() + " souls!");
    }
}