package com.oni.masks.commands;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.ui.ConfigGUI;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ConfigCommand implements CommandExecutor {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, 
                            @NotNull final String label, @NotNull final String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }
        
        final Player player = (Player) sender;
        
        if (!player.hasPermission("oni.admin.config")) {
            player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }
        
        // Open configuration GUI
        final ConfigGUI configGUI = new ConfigGUI();
        configGUI.openGUI(player);
        
        return true;
    }
}