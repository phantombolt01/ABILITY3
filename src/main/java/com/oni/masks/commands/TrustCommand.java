package com.oni.masks.commands;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.player.TrustManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class TrustCommand implements CommandExecutor {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final TrustManager trustManager = this.plugin.getTrustManager();
    
    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, 
                            @NotNull final String label, @NotNull final String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }
        
        final Player player = (Player) sender;
        
        if (args.length != 1) {
            player.sendMessage(Component.text("Usage: /" + label + " <player>", NamedTextColor.RED));
            return true;
        }
        
        final Player targetPlayer = this.plugin.getServer().getPlayer(args[0]);
        if (targetPlayer == null) {
            player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("trust")) {
            this.trustManager.trustPlayer(player, targetPlayer);
        } else if (command.getName().equalsIgnoreCase("untrust")) {
            this.trustManager.untrustPlayer(player, targetPlayer);
        }
        
        return true;
    }
}