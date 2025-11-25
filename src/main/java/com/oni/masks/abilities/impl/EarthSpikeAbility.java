package com.oni.masks.abilities.impl;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.config.PluginConfig;
import com.oni.masks.player.DamageUtils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class EarthSpikeAbility extends Ability implements Listener {

    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PluginConfig config = this.plugin.getConfigManager().getPluginConfig();
    private static final Set<Block> eventSpikeBlocks = new HashSet<>();

    // Tracks entities hit per cast
    private final Set<UUID> hitEntities = new HashSet<>();

    public EarthSpikeAbility(final Player player) {
        super("Earth Spike", player, 40);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public void execute() {
        final Location startLocation = this.player.getLocation();
        final Vector direction = this.player.getEyeLocation().getDirection();
        direction.setY(0); // Keep spikes flat
        direction.normalize();

        this.plugin.getParticleManager().playEarthSpikeAnimation(this.player);
        this.plugin.getSoundManager().playEarthSpikeSound(this.player);

        // Reset hit tracking for this cast
        hitEntities.clear();

        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (this.ticks >= 16) {
                    this.cancel();
                    return;
                }

                for (int i = 0; i < 3; i++) {
                    final double distance = 2 + (this.ticks * 0.7);
                    final double sideOffset = (i - 1) * 1.5;

                    final Vector spikeDirection = direction.clone();
                    final Vector sideVector = direction.clone().rotateAroundY(Math.PI / 2).multiply(sideOffset);

                    final Location spikeLocation = startLocation.clone()
                            .add(spikeDirection.multiply(distance))
                            .add(sideVector);

                    createDripstoneSpike(spikeLocation);
                    damageEntitiesAtSpike(spikeLocation);
                }

                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);

        this.sendAbilityMessage("§a§l[Earth Mask] §7Dripstone spikes pierce the earth!");
    }

    private void createDripstoneSpike(final Location location) {
        final List<Block> spikeBlocks = new ArrayList<>();

        Location groundLocation = location.clone();
        while (groundLocation.getY() > 0 &&
                groundLocation.getBlock().getType() == Material.AIR) {
            groundLocation.add(0, -1, 0);
        }

        for (int i = 1; i <= 3; i++) {
            final Block spikeBlock = groundLocation.clone().add(0, i, 0).getBlock();
            if (spikeBlock.getType() == Material.AIR) {
                spikeBlock.setType(Material.DRIPSTONE_BLOCK);
                spikeBlock.setMetadata("eventSpike", new FixedMetadataValue(this.plugin, true));
                spikeBlocks.add(spikeBlock);
                eventSpikeBlocks.add(spikeBlock);
            }
        }

        final Block supportBlock = groundLocation.clone().add(0, 3, 0).getBlock();
        final Block tipBlock = groundLocation.clone().add(0, 4, 0).getBlock();

        if (supportBlock.getType() == Material.AIR) {
            supportBlock.setType(Material.BARRIER);
            supportBlock.setMetadata("eventSpike", new FixedMetadataValue(this.plugin, true));
            spikeBlocks.add(supportBlock);
            eventSpikeBlocks.add(supportBlock);
        }

        if (tipBlock.getType() == Material.AIR) {
            tipBlock.setType(Material.POINTED_DRIPSTONE);
            tipBlock.setMetadata("eventSpike", new FixedMetadataValue(this.plugin, true));
            spikeBlocks.add(tipBlock);
            eventSpikeBlocks.add(tipBlock);
        }

        location.getWorld().spawnParticle(org.bukkit.Particle.BLOCK, location.clone().add(0, 2, 0),
                15, 0.5, 1, 0.5, 0.1, Material.DRIPSTONE_BLOCK.createBlockData());
        location.getWorld().spawnParticle(org.bukkit.Particle.FALLING_DUST, location.clone().add(0, 3, 0),
                8, 0.3, 0.5, 0.3, 0, Material.STONE.createBlockData());

        new BukkitRunnable() {
            @Override
            public void run() {
                for (final Block block : spikeBlocks) {
                    if (eventSpikeBlocks.contains(block)) {
                        eventSpikeBlocks.remove(block);
                        block.setType(Material.AIR);
                    }
                }
            }
        }.runTaskLater(this.plugin, 80L);
    }

    private void damageEntitiesAtSpike(final Location location) {
        for (final Entity entity : location.getWorld().getNearbyEntities(location, 2, 4, 2)) {
            if (entity instanceof LivingEntity && !entity.equals(this.player)) {
                final LivingEntity livingEntity = (LivingEntity) entity;

                if (hitEntities.contains(livingEntity.getUniqueId())) {
                    continue; // already damaged this cast
                }

                if (livingEntity instanceof Player) {
                    final Player targetPlayer = (Player) entity;
                    if (!this.plugin.getTrustManager().canHarm(this.player, targetPlayer)) {
                        continue;
                    }
                }

                hitEntities.add(livingEntity.getUniqueId());

                DamageUtils.applyAdaptiveDamage(livingEntity, 2, player);

                // ✅ Trigger red hurt animation
                livingEntity.damage(0.1, this.player); // fake hit, doesn’t reduce HP

DamageUtils.applyAdaptiveDamage(livingEntity, 2.5, player);

                // ✅ Trigger red hurt animation
                  livingEntity.damage(0.1, this.player); // fake hit, doesn’t reduce HP

                // ✅ Knock-up
                    final Vector knockup = new Vector(0, 3, 0);
                    livingEntity.setVelocity(livingEntity.getVelocity().add(knockup));

                // ✅ Disorient mid-air
                    livingEntity.addPotionEffect(new org.bukkit.potion.PotionEffect(
                      org.bukkit.potion.PotionEffectType.NAUSEA, 80, 0, false, false, true)); // nausea 4s
                    livingEntity.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.BLINDNESS, 80, 0, false, false, true)); // blindness 4s


                // ✅ Sound
                livingEntity.getWorld().playSound(livingEntity.getLocation(),
                        org.bukkit.Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);
            }
        }
    }

}
