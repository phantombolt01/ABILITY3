package com.oni.masks.commands;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.items.ItemManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class RerollCommand implements CommandExecutor {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final ItemManager itemManager = this.plugin.getItemManager();
    
    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, 
                            @NotNull final String label, @NotNull final String[] args) {
        
        if (!sender.hasPermission("oni.admin.reroll")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }
        
        Player targetPlayer;
        
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("Please specify a player when using from console!", NamedTextColor.RED));
                return true;
            }
            targetPlayer = (Player) sender;
        } else {
            targetPlayer = this.plugin.getServer().getPlayer(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return true;
            }
        }
        
        // Give reroll item to target player
        final ItemStack rerollItem = this.itemManager.createRerollItem();
        targetPlayer.getInventory().addItem(rerollItem);
        
        targetPlayer.sendMessage(Component.text("You have received a Mask Reroll Token!", NamedTextColor.GOLD));
        
        if (!sender.equals(targetPlayer)) {
            sender.sendMessage(Component.text("Gave reroll item to " + targetPlayer.getName(), NamedTextColor.GREEN));
        }
        
        return true;
    }
}