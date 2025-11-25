package com.oni.masks.abilities.impl;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FeastOfDecayAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public FeastOfDecayAbility(final Player player) {
        super("Feast of Decay", player, 40);
    }
    
    @Override
    public void execute() {
        final Location playerLocation = this.player.getLocation();
        int targetsHit = 0;
        
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
                
                targetsHit++;
                
                // Apply Poison II and Nausea for 7 seconds
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 140, 1));
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 140, 0));
                
                // Green toxic particles on target
                entity.getWorld().spawnParticle(org.bukkit.Particle.ITEM_SLIME, 
                        entity.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
                entity.getWorld().spawnParticle(org.bukkit.Particle.WITCH, 
                        entity.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.05);
            }
        }
        
        // Toxic fog around player
        playerLocation.getWorld().spawnParticle(org.bukkit.Particle.ITEM_SLIME, 
                playerLocation.add(0, 1, 0), 25, 3, 3, 3, 0.2);
        playerLocation.getWorld().spawnParticle(org.bukkit.Particle.WITCH, 
                playerLocation, 15, 2, 2, 2, 0.1);
        
        // Play disgusting sounds
        this.player.getWorld().playSound(playerLocation, org.bukkit.Sound.ENTITY_GENERIC_DRINK, 2.0f, 0.5f);
        this.player.getWorld().playSound(playerLocation, org.bukkit.Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 0.8f);
        
        this.sendAbilityMessage("§8§l[GLUTTONY] §7The feast of decay sickens " + targetsHit + " souls!");
    }
}