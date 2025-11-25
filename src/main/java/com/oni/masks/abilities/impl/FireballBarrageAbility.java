package com.oni.masks.abilities.impl;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.config.PluginConfig;
import com.oni.masks.player.DamageUtils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FireballBarrageAbility extends Ability {

    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PluginConfig config = this.plugin.getConfigManager().getPluginConfig();

    public FireballBarrageAbility(final Player player) {
        super("Fireball Barrage", player, 40);
    }

    @Override
    public void execute() {
        final Location eyeLocation = this.player.getEyeLocation();
        final Vector direction = eyeLocation.getDirection();

        // Play flame animation & sound
        this.plugin.getParticleManager().playFlameAnimation(this.player);
        this.plugin.getSoundManager().playFireballSound(this.player);

        // Find multiple targets in targeting cone for multi-targeting
        final List<LivingEntity> targets = this.findMultipleTargetsInCone(eyeLocation, direction, 15.0, Math.PI / 4);
        final int maxTargets = Math.min(targets.size(), 30); // Max 3 targets

        if (targets.isEmpty()) {
            // No targets found, fire in direction
            this.launchFireballInDirection(eyeLocation, direction);
            this.sendAbilityMessage("§c§l[Flame Mask] §7Fireballs streak through the air!");
            return;
        }

        // Launch homing fireballs at each target
        for (int i = 0; i < maxTargets; i++) {
            final LivingEntity target = targets.get(i);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    launchHomingFireball(eyeLocation, target);
                }
            }.runTaskLater(this.plugin, i * 3L); // Stagger launches by 3 ticks
        }

        this.sendAbilityMessage("§c§l[Flame Mask] §7" + maxTargets + " homing fireballs streak toward your enemies!");
    }

    private List<LivingEntity> findMultipleTargetsInCone(final Location eyeLocation, final Vector direction,
                                                         final double range, final double coneAngle) {
        final List<LivingEntity> validTargets = new ArrayList<>();

        for (final Entity entity : eyeLocation.getWorld().getNearbyEntities(eyeLocation, range, range, range)) {
            if (entity instanceof LivingEntity && !entity.equals(this.player)) {
                final LivingEntity livingEntity = (LivingEntity) entity;

                // Check if target can be harmed (trust check)
                if (entity instanceof Player) {
                    final Player targetPlayer = (Player) entity;
                    if (!this.plugin.getTrustManager().canHarm(this.player, targetPlayer)) {
                        continue;
                    }
                }

                // Check if target is within cone
                final Vector toTarget = livingEntity.getLocation().toVector()
                        .subtract(eyeLocation.toVector());
                if (toTarget.lengthSquared() == 0) continue; // safety
                toTarget.normalize();

                final double angle = direction.angle(toTarget);

                if (angle <= coneAngle) {
                    validTargets.add(livingEntity);
                }
            }
        }

        // Sort by distance (closest first) and return up to max targets
        validTargets.sort(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(eyeLocation)));
        
        return validTargets;
    }

    private void launchHomingFireball(final Location eyeLocation, final LivingEntity target) {
        final Vector direction = target.getLocation().toVector()
                .subtract(eyeLocation.toVector()).normalize();

        // Spawn fireball a bit in front of eyes
        final Fireball fireball = this.player.getWorld().spawn(
                eyeLocation.clone().add(direction.clone().multiply(2)),
                Fireball.class
        );

        // Ensure velocity and shooter
        fireball.setVelocity(direction.normalize().multiply(1.2));
        fireball.setYield(0.8f); // Reduced explosion power
        fireball.setShooter(this.player);

        // Add homing behavior
        this.startHomingBehavior(fireball, target);
    }

    private void launchFireballInDirection(final Location eyeLocation, final Vector direction) {
        final Fireball fireball = this.player.getWorld().spawn(
                eyeLocation.clone().add(direction.clone().multiply(2)),
                Fireball.class
        );

        fireball.setVelocity(direction.normalize().multiply(1.2));
        fireball.setYield(0.8f);
        fireball.setShooter(this.player);
    }

    private void startHomingBehavior(final Fireball fireball, final LivingEntity target) {
        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (fireball.isDead() || target.isDead() || this.ticks > 600) { // 3 second max homing
                    this.cancel();
                    return;
                }

                // Calculate homing direction
                final Vector toTarget = target.getLocation().toVector()
                        .subtract(fireball.getLocation().toVector());
                if (toTarget.lengthSquared() > 0) {
                    toTarget.normalize().multiply(0.3); // Homing strength

                    // Apply gentle course correction
                    final Vector currentVelocity = fireball.getVelocity();
                    final Vector newVelocity = currentVelocity.add(toTarget).normalize().multiply(currentVelocity.length());
                    fireball.setVelocity(newVelocity);
                }

                // Add homing particles
                fireball.getWorld().spawnParticle(org.bukkit.Particle.FLAME, fireball.getLocation(), 2, 0.1, 0.1, 0.1, 0.02);

                // Check for close proximity hit
                if (fireball.getLocation().distance(target.getLocation()) < 2.0) {
                    DamageUtils.applyAdaptiveDamage(target, 3, player); // → always deals 3 hearts after armor/prot/totems

                                target.setFireTicks(60); // 3 seconds of fire
                    
                    // Apply regen suppression
                    if (target instanceof Player) {
                        final Player targetPlayer = (Player) target;
                        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, -40, -1)); // Remove regen
                        targetPlayer.sendMessage("§cRegen suppressed!");
                        
                        // Apply temporary regen lock
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (targetPlayer.isOnline()) {
                                    targetPlayer.sendMessage("§aRegen restored.");
                                }
                            }
                        }.runTaskLater(plugin, 40L); // 2 seconds
                    }
                    
                    // Create explosion effect
                    fireball.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, fireball.getLocation(), 1, 0, 0, 0, 0);
                    fireball.getWorld().spawnParticle(org.bukkit.Particle.LAVA, fireball.getLocation(), 8, 1, 1, 1, 0.1);
                    
                    fireball.remove();
                    this.cancel();
                }

                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 1L, 2L); // Every 2 ticks
    }
}