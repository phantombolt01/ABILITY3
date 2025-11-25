package com.oni.masks.commands;

import com.oni.masks.OniMasksPlugin;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class PluginInfoCommand implements CommandExecutor {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, 
                            @NotNull final String label, @NotNull final String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }
        
        final Player player = (Player) sender;
        
        // Send plugin info messages
        player.sendMessage(Component.text("§6§lOni S1 — Plugin Info"));
        player.sendMessage(Component.text("§7Version: §a14.0.0 (Final RC)"));
        player.sendMessage(Component.text("§7Author: §bphantomxdz §7/ §bUtaib"));
        player.sendMessage(Component.text("§7Maintainer: §bUtaib "));
        player.sendMessage(Component.text("§7Help Me: §bPlease subscribe to the goat (me) @@Utaib hehe and also danobam he cool"));
        player.sendMessage(Component.text("§7Description: §fMask-based abilities for Oni SMP."));
        player.sendMessage(Component.text("§7Commands: §e/trust §7§e/untrust §7§e/event §7§e/reroll §7§e/config §7§e/plugininfo"));
        player.sendMessage(Component.text("§7Note: §fSome admin features require permission: oni.admin"));
        
        
        if (player.isInWaterOrRain()) {
            player.sendMessage(Component.text("§dEaster Egg: You've found the hidden sigil. Respect the Oni.", NamedTextColor.LIGHT_PURPLE));
            
            // Special lightning effect for easter egg
            if (Math.random() < 0.5) { // 30% chance
                player.getWorld().strikeLightningEffect(player.getLocation());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
            }
        }
        
        // Spawn particle easter egg around player
        this.spawnParticleShow(player);
        
        // Log usage for admin audit
        this.plugin.getLogger().info(player.getName() + " used /plugininfo command");
        
        return true;
    }
    
    private void spawnParticleShow(final Player player) {
        new BukkitRunnable() {
            private int ticks = 0;
            private final int duration = 40; // 2 seconds
            
            @Override
            public void run() {
                if (this.ticks >= duration) {
                    this.cancel();
                    return;
                }
                
                final var location = player.getLocation().add(0, 1, 0);
                
                // Rotating golden sparkles in a ring
                final double radius = 1.5;
                final double angle = (this.ticks * 0.3) % (2 * Math.PI);
                
                for (int i = 0; i < 5; i++) {
                    final double ringAngle = angle + (i * 2 * Math.PI / 5);
                    final double x = radius * Math.cos(ringAngle);
                    final double z = radius * Math.sin(ringAngle);
                    
                    location.getWorld().spawnParticle(org.bukkit.Particle.CRIT, 
                            location.clone().add(x, 0, z), 1, 0, 0, 0, 0);
                }
                
                // Vertical spiral of flame particles
                final double height = Math.sin(this.ticks * 0.2) * 1.5;
                location.getWorld().spawnParticle(org.bukkit.Particle.FLAME, 
                        location.clone().add(0, height, 0), 1, 0.1, 0.1, 0.1, 0.02);
                
                // Play soft sounds periodically
                if (this.ticks % 10 == 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.3f, 1.0f + (this.ticks * 0.05f));
                }
                
                this.ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }
}