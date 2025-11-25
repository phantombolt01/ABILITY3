package com.oni.masks.commands;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.sins.SinManager;
import com.oni.masks.sins.SinType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SosCommand implements CommandExecutor, TabCompleter {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final SinManager sinManager = this.plugin.getSinManager();
    
    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, 
                            @NotNull final String label, @NotNull final String[] args) {
        
        if (!sender.hasPermission("oni.admin.sos")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            this.sendUsage(sender);
            return true;
        }
        
        final String subCommand = args[0].toLowerCase();
        
        return switch (subCommand) {
            case "give" -> this.handleGive(sender, args);
            case "remove" -> this.handleRemove(sender, args);
            default -> {
                this.sendUsage(sender);
                yield true;
            }
        };
    }
    
    private boolean handleGive(final CommandSender sender, final String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /sos give <player> <sin>", NamedTextColor.RED));
            sender.sendMessage(Component.text("Available sins: pride, wrath, envy, greed, lust, gluttony, sloth", NamedTextColor.GRAY));
            return true;
        }
        
        final Player targetPlayer = this.plugin.getServer().getPlayer(args[1]);
        if (targetPlayer == null) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }
        
        final String sinName = args[2].toLowerCase();
        final SinType sinType = this.parseSinType(sinName);
        
        if (sinType == null) {
            sender.sendMessage(Component.text("Invalid sin type! Available: pride, wrath, envy, greed, lust, gluttony, sloth", NamedTextColor.RED));
            return true;
        }
        
        // Assign the sin
        this.sinManager.assignSin(targetPlayer, sinType);
        
        // Send confirmation messages
        sender.sendMessage(Component.text("[Admin] You have cursed " + targetPlayer.getName() + " with the Sin of " + sinType.getDisplayName(), NamedTextColor.GREEN));
        
        return true;
    }
    
    private boolean handleRemove(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /sos remove <player>", NamedTextColor.RED));
            return true;
        }
        
        final Player targetPlayer = this.plugin.getServer().getPlayer(args[1]);
        if (targetPlayer == null) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }
        
        // Remove the sin
        this.sinManager.removeSin(targetPlayer);
        
        // Send confirmation message
        sender.sendMessage(Component.text("[Admin] You have lifted the sin from " + targetPlayer.getName(), NamedTextColor.GREEN));
        
        return true;
    }
    
    private SinType parseSinType(final String sinName) {
        return switch (sinName) {
            case "pride" -> SinType.PRIDE;
            case "wrath" -> SinType.WRATH;
            case "envy" -> SinType.ENVY;
            case "greed" -> SinType.GREED;
            case "lust" -> SinType.LUST;
            case "gluttony" -> SinType.GLUTTONY;
            case "sloth" -> SinType.SLOTH;
            default -> null;
        };
    }
    
    private void sendUsage(final CommandSender sender) {
        sender.sendMessage(Component.text("Season of Sins Commands:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/sos give <player> <sin> - Curse a player with a sin", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/sos remove <player> - Remove a sin from a player", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Available sins: pride, wrath, envy, greed, lust, gluttony, sloth", NamedTextColor.GRAY));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, 
                                               @NotNull final String alias, @NotNull final String[] args) {
        
        final List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("give");
            completions.add("remove");
        } else if (args.length == 2) {
            return this.plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            completions.add("pride");
            completions.add("wrath");
            completions.add("envy");
            completions.add("greed");
            completions.add("lust");
            completions.add("gluttony");
            completions.add("sloth");
        }
        
        return completions;
    }
}