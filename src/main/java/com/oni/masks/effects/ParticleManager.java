package com.oni.masks.effects;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.config.PluginConfig;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public class ParticleManager {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public void playFlameAnimation(final Player player) {
        final Location location = player.getLocation().add(0, 2.5, 0);
        final PluginConfig config = this.plugin.getConfigManager().getPluginConfig();
        final int count = (int) (30 * config.getParticleIntensity());
        
        // Create flame spiral above player
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 20) {
                    this.cancel();
                    return;
                }
                
                final double radius = 1.5;
                final double height = 0.1 * this.ticks;
                
                for (int i = 0; i < 8; i++) {
                    final double angle = (2 * Math.PI * i / 8) + (this.ticks * 0.3);
                    final double x = radius * Math.cos(angle);
                    final double z = radius * Math.sin(angle);
                    
                    final Location particleLocation = location.clone().add(x, height, z);
                    location.getWorld().spawnParticle(Particle.FLAME, particleLocation, 1, 0, 0, 0, 0.02);
                    location.getWorld().spawnParticle(Particle.LAVA, particleLocation, 1, 0, 0, 0, 0);
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    public void playEarthSpikeAnimation(final Player player) {
        final Location location = player.getLocation().add(0, 2, 0);
        final PluginConfig config = this.plugin.getConfigManager().getPluginConfig();
        
        // Dripstone-style spike animation (3.2.0 upgrade)
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 30) {
                    this.cancel();
                    return;
                }
                
                // Create rising dripstone-like spikes
                final double radius = 2.0;
                final int spikeCount = 8;
                
                for (int i = 0; i < spikeCount; i++) {
                    final double angle = (2 * Math.PI * i / spikeCount) + (this.ticks * 0.1);
                    final double x = radius * Math.cos(angle);
                    final double z = radius * Math.sin(angle);
                    final double height = Math.sin(this.ticks * 0.2) * 2.0;
                    
                    final Location spikeLocation = location.clone().add(x, height, z);
                    
                    // Dripstone spike particles
                    spikeLocation.getWorld().spawnParticle(Particle.BLOCK, spikeLocation, 2, 0.1, 0.1, 0.1, 0,
                            org.bukkit.Material.DRIPSTONE_BLOCK.createBlockData());
                    
                    // Falling dust effect
                    spikeLocation.getWorld().spawnParticle(Particle.FALLING_DUST, spikeLocation.clone().add(0, 1, 0), 1, 0.1, 0.1, 0.1, 0,
                            org.bukkit.Material.STONE.createBlockData());
                    
                    // Sharp crystal-like particles
                    spikeLocation.getWorld().spawnParticle(Particle.CRIT, spikeLocation, 1, 0.1, 0.1, 0.1, 0.1);
                }
                
                // Center impact effect
                location.getWorld().spawnParticle(Particle.BLOCK, location, 3, 0.3, 0.1, 0.3, 0.1,
                        org.bukkit.Material.COBBLESTONE.createBlockData());
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L); // Smoother animation
    }
    
    public void playEarthShieldAnimation(final Player player) {
        final Location location = player.getLocation();
        
        // Create protective earth aura
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 40) {
                    this.cancel();
                    return;
                }
                
                final double radius = 2.5;
                final int points = 16;
                
                for (int i = 0; i < points; i++) {
                    final double angle = (2 * Math.PI * i / points) + (this.ticks * 0.1);
                    final double x = radius * Math.cos(angle);
                    final double z = radius * Math.sin(angle);
                    
                    final Location particleLocation = location.clone().add(x, 1, z);
                    location.getWorld().spawnParticle(Particle.BLOCK, particleLocation, 1, 
                            org.bukkit.Material.MOSSY_COBBLESTONE.createBlockData());
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    public void playWaterWaveAnimation(final Player player) {
        final Location location = player.getLocation().add(0, 2, 0);
        
        // Water wave effect above player
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 25) {
                    this.cancel();
                    return;
                }
                
                final double radius = 2.0;
                final double height = Math.sin(this.ticks * 0.3) * 0.5;
                
                for (int i = 0; i < 20; i++) {
                    final double angle = (2 * Math.PI * i / 20);
                    final double x = radius * Math.cos(angle);
                    final double z = radius * Math.sin(angle);
                    
                    final Location particleLocation = location.clone().add(x, height, z);
                    location.getWorld().spawnParticle(Particle.DRIPPING_WATER, particleLocation, 2, 0.1, 0.1, 0.1, 0);
                    location.getWorld().spawnParticle(Particle.BUBBLE_POP, particleLocation, 1, 0, 0, 0, 0);
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    public void playWaterWaveParticles(final Location location) {
        // Enhanced water wave particles with foam trails
        location.getWorld().spawnParticle(Particle.SPLASH, location, 15, 1.5, 0.5, 1.5, 0.2);
        location.getWorld().spawnParticle(Particle.BUBBLE_POP, location, 8, 0.8, 0.5, 0.8, 0.1);
        location.getWorld().spawnParticle(Particle.DRIPPING_WATER, location.clone().add(0, 1, 0), 5, 1, 0.5, 1, 0);
        
        // White foam trails
        location.getWorld().spawnParticle(Particle.CLOUD, location, 3, 0.5, 0.2, 0.5, 0.02);
    }
    
    public void playHealingAuraAnimation(final Player player) {
        final Location location = player.getLocation().add(0, 2, 0);
        
        // Healing light orb above player
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 100) {
                    this.cancel();
                    return;
                }
                
                // Pulsing light orb
                final double intensity = Math.sin(this.ticks * 0.2) * 0.5 + 1;
                location.getWorld().spawnParticle(Particle.END_ROD, location, (int) (5 * intensity), 0.3, 0.3, 0.3, 0.02);
                location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 2, 0.2, 0.2, 0.2, 0);
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 2L);
    }
    
    public void playHealingParticles(final Location location) {
        location.getWorld().spawnParticle(Particle.HEART, location, 3, 0.5, 1, 0.5, 0);
        location.getWorld().spawnParticle(Particle.END_ROD, location, 2, 0.2, 0.5, 0.2, 0.02);
    }
    
    public void playLightBallAnimation(final Player player) {
        final Location location = player.getLocation().add(0, 2.5, 0);
        
        // Light orb formation
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 15) {
                    this.cancel();
                    return;
                }
                
                location.getWorld().spawnParticle(Particle.END_ROD, location, 8, 0.5, 0.5, 0.5, 0.1);
                location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 3, 0.3, 0.3, 0.3, 0.05);
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    public void playLightBallTrail(final Location location) {
        location.getWorld().spawnParticle(Particle.END_ROD, location, 1, 0, 0, 0, 0);
        location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 1, 0, 0, 0, 0);
    }
    
    public void playLightExplosion(final Location location) {
        location.getWorld().spawnParticle(Particle.END_ROD, location, 20, 1, 1, 1, 0.2);
        location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 15, 0.8, 0.8, 0.8, 0.1);
        location.getWorld().spawnParticle(Particle.FLASH, location, 1, 0, 0, 0, 0);
    }
    
    // Event Mask Particles
    public void playEventMaskActivation(final Player player) {
        final Location location = player.getLocation().add(0, 1, 0);
        
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 60) {
                    this.cancel();
                    return;
                }
                
                // Dark energy swirling around player
                final double radius = 3.0;
                final int points = 12;
                
                for (int i = 0; i < points; i++) {
                    final double angle = (2 * Math.PI * i / points) + (this.ticks * 0.2);
                    final double x = radius * Math.cos(angle);
                    final double z = radius * Math.sin(angle);
                    final double y = Math.sin(this.ticks * 0.1) * 2;
                    
                    final Location particleLocation = location.clone().add(x, y, z);
                    location.getWorld().spawnParticle(Particle.SOUL, particleLocation, 2, 0.1, 0.1, 0.1, 0.02);
                    location.getWorld().spawnParticle(Particle.SMOKE, particleLocation, 1, 0, 0, 0, 0.01);
                }
                
                // Center dark orb
                location.getWorld().spawnParticle(Particle.PORTAL, location, 10, 0.3, 0.3, 0.3, 0.5);
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    public void playPrimordialFlameActivation(final Player player) {
        final Location location = player.getLocation().add(0, 1, 0);
        
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 80) { // 4 seconds
                    this.cancel();
                    return;
                }
                
                // Intense flame spiral around player
                final double radius = 4.0;
                final int points = 20;
                
                for (int i = 0; i < points; i++) {
                    final double angle = (2 * Math.PI * i / points) + (this.ticks * 0.3);
                    final double x = radius * Math.cos(angle);
                    final double z = radius * Math.sin(angle);
                    final double y = Math.sin(this.ticks * 0.2) * 3;
                    
                    final Location particleLocation = location.clone().add(x, y, z);
                    location.getWorld().spawnParticle(Particle.FLAME, particleLocation, 3, 0.2, 0.2, 0.2, 0.05);
                    location.getWorld().spawnParticle(Particle.LAVA, particleLocation, 1, 0.1, 0.1, 0.1, 0);
                }
                
                // Central fire pillar
                location.getWorld().spawnParticle(Particle.FLAME, location, 15, 0.5, 1, 0.5, 0.1);
                location.getWorld().spawnParticle(Particle.SMOKE, location, 8, 0.3, 0.5, 0.3, 0.05);
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    public void playPrimordialFlameOrb(final Player player) {
        final Location location = player.getLocation().add(0, 2, 0);
        
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 60) { // 3 seconds for premium effect
                    this.cancel();
                    return;
                }
                
                // Premium massive flame orb formation (final visual overhaul)
                final double intensity = Math.sin(this.ticks * 0.3) * 0.5 + 1.5;
                location.getWorld().spawnParticle(Particle.FLAME, location, (int) (30 * intensity), 1.5, 1.5, 1.5, 0.15);
                location.getWorld().spawnParticle(Particle.LAVA, location, (int) (15 * intensity), 1.2, 1.2, 1.2, 0.08);
                location.getWorld().spawnParticle(Particle.SMOKE, location, (int) (8 * intensity), 0.8, 0.8, 0.8, 0.05);
                location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location, (int) (5 * intensity), 0.5, 0.5, 0.5, 0.02);
                
                // Expanding rings
                if (this.ticks % 5 == 0) {
                    final double radius = 3.0 + (this.ticks * 0.15); // Larger rings for 15-block reach
                    for (int i = 0; i < 16; i++) {
                        final double angle = (2 * Math.PI * i / 16);
                        final double x = radius * Math.cos(angle);
                        final double z = radius * Math.sin(angle);
                        
                        final Location ringLocation = location.clone().add(x, 0, z);
                        location.getWorld().spawnParticle(Particle.FLAME, ringLocation, 3, 0.2, 0.2, 0.2, 0.05);
                        location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, ringLocation, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    // Void Mask Particles
    public void playVoidActivation(final Player player) {
        final Location location = player.getLocation().add(0, 1, 0);
        
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 60) {
                    this.cancel();
                    return;
                }
                
                // Dark void energy swirling around player
                final double radius = 3.0;
                final int points = 16;
                
                for (int i = 0; i < points; i++) {
                    final double angle = (2 * Math.PI * i / points) + (this.ticks * 0.15);
                    final double x = radius * Math.cos(angle);
                    final double z = radius * Math.sin(angle);
                    final double y = Math.sin(this.ticks * 0.1) * 2;
                    
                    final Location particleLocation = location.clone().add(x, y, z);
                    location.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLocation, 2, 0.1, 0.1, 0.1, 0.02);
                    location.getWorld().spawnParticle(Particle.SQUID_INK, particleLocation, 1, 0, 0, 0, 0.01);
                }
                
                // Center void orb
                location.getWorld().spawnParticle(Particle.PORTAL, location, 15, 0.3, 0.3, 0.3, 0.8);
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    public void playVoidGrasp(final Player player) {
        final Location location = player.getLocation().add(0, 1, 0);
        
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 40) {
                    this.cancel();
                    return;
                }
                
                // Inward spiraling void rift
                final double radius = 4.0 - (this.ticks * 0.05);
                for (int i = 0; i < 20; i++) {
                    final double angle = (2 * Math.PI * i / 20) - (this.ticks * 0.2);
                    final double x = radius * Math.cos(angle);
                    final double z = radius * Math.sin(angle);
                    
                    final Location particleLocation = location.clone().add(x, 0, z);
                    location.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLocation, 3, 0.1, 0.1, 0.1, 0.02);
                    location.getWorld().spawnParticle(Particle.SQUID_INK, particleLocation, 2, 0.2, 0.2, 0.2, 0.01);
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    public void playVoidEruption(final Player player) {
        final Location location = player.getLocation().add(0, 1, 0);
        
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 20) {
                    this.cancel();
                    return;
                }
                
                // Explosive void energy outward
                final double radius = this.ticks * 0.5;
                for (int i = 0; i < 24; i++) {
                    final double angle = (2 * Math.PI * i / 24);
                    final double x = radius * Math.cos(angle);
                    final double z = radius * Math.sin(angle);
                    
                    final Location particleLocation = location.clone().add(x, 0, z);
                    location.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLocation, 4, 0.2, 0.2, 0.2, 0.05);
                    location.getWorld().spawnParticle(Particle.SOUL, particleLocation, 2, 0.1, 0.1, 0.1, 0.02);
                }
                
                // Central explosion
                location.getWorld().spawnParticle(Particle.EXPLOSION, location, 2, 0, 0, 0, 0);
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    public void playVoidDamageEffect(final Location location) {
        location.getWorld().spawnParticle(Particle.DRAGON_BREATH, location, 5, 0.3, 0.5, 0.3, 0.02);
        location.getWorld().spawnParticle(Particle.SQUID_INK, location, 3, 0.2, 0.2, 0.2, 0.01);
    }
    
    public void playVoidExplosionEffect(final Location location) {
        location.getWorld().spawnParticle(Particle.DRAGON_BREATH, location, 10, 0.5, 0.5, 0.5, 0.1);
        location.getWorld().spawnParticle(Particle.SOUL, location, 8, 0.4, 0.4, 0.4, 0.05);
        location.getWorld().spawnParticle(Particle.SMOKE, location, 5, 0.3, 0.3, 0.3, 0.02);
    }
    
    // Lightning Mask Particles
    public void playLightningActivation(final Player player) {
        final Location location = player.getLocation().add(0, 1, 0);
        
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 60) {
                    this.cancel();
                    return;
                }
                
                // Electric energy crackling around player
                final double radius = 2.5;
                final int points = 12;
                
                for (int i = 0; i < points; i++) {
                    final double angle = (2 * Math.PI * i / points) + (this.ticks * 0.3);
                    final double x = radius * Math.cos(angle);
                    final double z = radius * Math.sin(angle);
                    final double y = Math.sin(this.ticks * 0.2) * 1.5;
                    
                    final Location particleLocation = location.clone().add(x, y, z);
                    location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, particleLocation, 3, 0.1, 0.1, 0.1, 0.05);
                    location.getWorld().spawnParticle(Particle.CRIT, particleLocation, 2, 0.1, 0.1, 0.1, 0.02);
                }
                
                // Central lightning core
                location.getWorld().spawnParticle(Particle.END_ROD, location, 8, 0.2, 0.2, 0.2, 0.1);
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    public void playStormCall(final Player player) {
        final Location location = player.getLocation().add(0, 3, 0);
        
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 60) {
                    this.cancel();
                    return;
                }
                
                // Storm clouds gathering
                for (int i = 0; i < 30; i++) {
                    final double x = (Math.random() - 0.5) * 20;
                    final double z = (Math.random() - 0.5) * 20;
                    final double y = Math.random() * 5;
                    
                    final Location cloudLocation = location.clone().add(x, y, z);
                    location.getWorld().spawnParticle(Particle.CLOUD, cloudLocation, 1, 0.1, 0.1, 0.1, 0.02);
                    location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, cloudLocation, 2, 0.2, 0.2, 0.2, 0.1);
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 2L);
    }
    
    public void playLightningBurst(final Player player) {
        final Location location = player.getLocation().add(0, 1, 0);
        
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 20) {
                    this.cancel();
                    return;
                }
                
                // Radial lightning explosion
                final double radius = this.ticks * 0.4;
                for (int i = 0; i < 16; i++) {
                    final double angle = (2 * Math.PI * i / 16);
                    final double x = radius * Math.cos(angle);
                    final double z = radius * Math.sin(angle);
                    
                    final Location sparkLocation = location.clone().add(x, 0, z);
                    location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, sparkLocation, 4, 0.1, 0.1, 0.1, 0.1);
                    location.getWorld().spawnParticle(Particle.CRIT, sparkLocation, 2, 0.1, 0.1, 0.1, 0.05);
                }
                
                // Central lightning pillar
                location.getWorld().spawnParticle(Particle.END_ROD, location, 10, 0.3, 1, 0.3, 0.1);
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    public void playLightningStrike(final Location location) {
        location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 8, 0.3, 0.5, 0.3, 0.1);
        location.getWorld().spawnParticle(Particle.CRIT, location, 5, 0.2, 0.2, 0.2, 0.05);
        location.getWorld().spawnParticle(Particle.END_ROD, location, 3, 0.1, 0.1, 0.1, 0.02);
    }
    
    public void playLightningExplosion(final Location location) {
        location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 20, 1, 1, 1, 0.2);
        location.getWorld().spawnParticle(Particle.END_ROD, location, 15, 0.8, 0.8, 0.8, 0.1);
        location.getWorld().spawnParticle(Particle.FLASH, location, 2, 0, 0, 0, 0);
    }
    
    public void playLightningImpact(final Location location) {
        location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 12, 0.5, 0.5, 0.5, 0.15);
        location.getWorld().spawnParticle(Particle.CRIT, location, 8, 0.3, 0.3, 0.3, 0.1);
    }
    
    public void playEventActivationParticles(final Location location) {
        // Thunder and lightning effect
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 40) {
                    this.cancel();
                    return;
                }
                
                // Lightning strikes around location
                for (int i = 0; i < 3; i++) {
                    final Location strikeLocation = location.clone().add(
                            (Math.random() - 0.5) * 10,
                            10,
                            (Math.random() - 0.5) * 10
                    );
                    
                    // Vertical lightning beam
                    for (double y = 0; y <= 10; y += 0.5) {
                        final Location beamLocation = strikeLocation.clone().subtract(0, y, 0);
                        location.getWorld().spawnParticle(Particle.END_ROD, beamLocation, 1, 0, 0, 0, 0);
                        location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, beamLocation, 2, 0.1, 0.1, 0.1, 0);
                    }
                }
                
                // Dark energy explosion
                location.getWorld().spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
                location.getWorld().spawnParticle(Particle.SOUL, location, 20, 3, 3, 3, 0.1);
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 5L);
    }
    
    public void playEventMobSpawn(final Location location) {
        // Portal opening effect
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (this.ticks >= 30) {
                    this.cancel();
                    return;
                }
                
                final double radius = 2.0 + (this.ticks * 0.1);
                for (int i = 0; i < 16; i++) {
                    final double angle = (2 * Math.PI * i / 16);
                    final double x = radius * Math.cos(angle);
                    final double z = radius * Math.sin(angle);
                    
                    final Location particleLocation = location.clone().add(x, 0.1, z);
                    location.getWorld().spawnParticle(Particle.PORTAL, particleLocation, 3, 0, 0, 0, 1);
                    location.getWorld().spawnParticle(Particle.SMOKE, particleLocation, 1, 0, 0, 0, 0.05);
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
    
    public void playPoisonEffect(final Location location) {
        location.getWorld().spawnParticle(Particle.ITEM_SLIME, location, 5, 0.5, 1, 0.5, 0);
        location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location, 3, 0.3, 0.3, 0.3, 0);
    }
    
    public void playShadowBeam(final Location start, final Location end) {
        final Vector direction = end.toVector().subtract(start.toVector()).normalize();
        final double distance = start.distance(end);
        
        new BukkitRunnable() {
            private double traveled = 0;
            
            @Override
            public void run() {
                if (this.traveled >= distance) {
                    this.cancel();
                    return;
                }
                
                final Location beamLocation = start.clone().add(direction.clone().multiply(this.traveled));
                beamLocation.getWorld().spawnParticle(Particle.SOUL, beamLocation, 3, 0.1, 0.1, 0.1, 0.02);
                beamLocation.getWorld().spawnParticle(Particle.SMOKE, beamLocation, 1, 0, 0, 0, 0.01);
                
                this.traveled += 0.5;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
        
        // Impact effect
        new BukkitRunnable() {
            @Override
            public void run() {
                end.getWorld().spawnParticle(Particle.EXPLOSION, end, 1, 0, 0, 0, 0);
                end.getWorld().spawnParticle(Particle.SOUL, end, 10, 1, 1, 1, 0.1);
            }
        }.runTaskLater(this.plugin, (long) (distance * 2));
    }
}