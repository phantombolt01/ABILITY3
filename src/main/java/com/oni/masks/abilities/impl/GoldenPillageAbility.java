package com.oni.masks.abilities.impl;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GoldenPillageAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public GoldenPillageAbility(final Player player) {
        super("Golden Pillage", player, 45);
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
                
                // Drop gold nuggets
                final int goldAmount = 1 + (int) (Math.random() * 2); // 1-2 gold nuggets
                for (int i = 0; i < goldAmount; i++) {
                    final ItemStack gold = new ItemStack(Material.GOLD_NUGGET);
                    final Item droppedGold = entity.getWorld().dropItem(entity.getLocation(), gold);
                    droppedGold.setPickupDelay(20); // 1 second delay
                }
                
                // Golden explosion particles on target
                entity.getWorld().spawnParticle(org.bukkit.Particle.CRIT, 
                        entity.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.2);
                entity.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, 
                        entity.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0.1);
            }
        }
        
        // Give player Absorption II for 10 seconds
        this.player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 1));
        
        // Golden explosion around player
        playerLocation.getWorld().spawnParticle(org.bukkit.Particle.CRIT, 
                playerLocation.add(0, 1, 0), 30, 3, 3, 3, 0.3);
        playerLocation.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, 
                playerLocation, 20, 2, 2, 2, 0.2);
        
        // Play greedy sounds
        this.player.getWorld().playSound(playerLocation, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0f, 0.8f);
        this.player.getWorld().playSound(playerLocation, org.bukkit.Sound.BLOCK_ANVIL_LAND, 0.5f, 1.5f);
        
        this.sendAbilityMessage("§6§l[GREED] §7Golden pillage claims " + targetsHit + " victims!");
    }
}