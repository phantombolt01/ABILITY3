package com.oni.masks.abilities.impl;

import com.oni.masks.items.ItemManager;
import com.oni.masks.events.MobProgressionManager;
import com.oni.masks.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EventMaskAbility {

    private final MobProgressionManager mobProgressionManager;
    private final ItemManager itemManager;

    public EventMaskAbility(MobProgressionManager mobProgressionManager, ItemManager itemManager) {
        this.mobProgressionManager = mobProgressionManager;
        this.itemManager = itemManager;
    }

    public void activate(Player player, ItemStack handItem) {
        if (handItem == null) return;

        if (itemManager.isEventMask(handItem)) {
            // Get current stage safely
            int currentStage = mobProgressionManager.getPlayerCurrentStage(player.getUniqueId());
            int nextStage = Math.min(currentStage + 1, 5); // cap at 5

            // Summon mobs for next stage
            mobProgressionManager.summonMobsForStage(player, nextStage);
            mobProgressionManager.setPlayerCurrentStage(player.getUniqueId(), nextStage);

            // Update PlayerData XP & stage
            PlayerData playerData = mobProgressionManager.getPlayerDataManager().getPlayerData(player.getUniqueId());
            playerData.setCurrentEventStage(nextStage);
            playerData.setEventStageXP(0);

            // Notify player
            player.sendMessage(Component.text("§cYou have summoned the next stage of Forbidden Shadows!", NamedTextColor.RED));
        }
    }
}
