package com.oni.masks.listeners;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.masks.Mask;
import com.oni.masks.player.PlayerData;
import com.oni.masks.player.PlayerDataManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

@RequiredArgsConstructor
public class PlayerInteractListener implements Listener {

    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        final Action action = event.getAction();
        final ItemStack item = event.getItem();

        // Handle reroll item usage
        if (item != null && this.plugin.getItemManager().isRerollItem(item)) {
            this.handleRerollItem(player, item, action);
            event.setCancelled(true);
            return;
        }
    }

    private void handleRerollItem(final Player player, final ItemStack item, final Action action) {
        // Only allow right-click to use reroll
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        final PlayerData playerData = this.playerDataManager.getPlayerData(player.getUniqueId());

        // Cannot reroll event masks
        if (playerData.getMaskType() != null && playerData.getMaskType().isEventMask()) {
            player.sendMessage(Component.text("§cEvent masks cannot be rerolled!", NamedTextColor.RED));
            return;
        }

        // Check cooldown (reduced to 20 seconds)
        if (!playerData.canUseReroll()) {
            final long remainingTime = (playerData.getLastRerollTime() + 20000 - System.currentTimeMillis()) / 1000;
            player.sendMessage(Component.text("§cYou must wait " + remainingTime + "s before rerolling again!", NamedTextColor.RED));
            return;
        }

        // Remove one reroll item
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }

        // Set cooldown
        playerData.setLastRerollTime(System.currentTimeMillis());

        // Reroll the mask
        this.plugin.getMaskManager().rerollMask(player);

        // Play reroll sound
        this.plugin.getSoundManager().playRerollSound(player);

        // Particle glow burst at player's feet
        player.getWorld().spawnParticle(org.bukkit.Particle.END_ROD,
                player.getLocation().add(0, 0.1, 0), 15, 0.5, 0.1, 0.5, 0.1);
        player.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK,
                player.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);

        // Save data
        this.playerDataManager.savePlayerData(player.getUniqueId());

        // Send ready message after cooldown
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.sendMessage(Component.text("§aReroll available again!", NamedTextColor.GREEN));
                }
            }
        }.runTaskLater(this.plugin, 400L); // 20 seconds
    }
}
