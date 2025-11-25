package com.oni.masks.commands;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.masks.MaskType;
import com.oni.masks.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventCommand implements CommandExecutor, TabCompleter {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final Map<UUID, Long> confirmationTimestamps = new HashMap<>();
    private final Map<UUID, String> pendingCommands = new HashMap<>();
    
    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, 
                            @NotNull final String label, @NotNull final String[] args) {
        
        if (args.length == 0) {
            this.sendUsage(sender);
            return true;
        }
        
        final String subCommand = args[0].toLowerCase();
        
        return switch (subCommand) {
            case "give" -> this.handleGive(sender, args);
            case "remove" -> this.handleRemove(sender, args);
            case "transfer" -> this.handleTransfer(sender, args);
            case "delete" -> this.handleDelete(sender, args);
            default -> {
                this.sendUsage(sender);
                yield true;
            }
        };
    }
    
    private boolean handleGive(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission("oni.admin.event")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /event give <player> <mask>", NamedTextColor.RED));
            sender.sendMessage(Component.text("Available masks: forbidden_shadows, primordial_flame, void, lightning", NamedTextColor.GRAY));
            return true;
        }
        
        final Player targetPlayer = this.plugin.getServer().getPlayer(args[1]);
        if (targetPlayer == null) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }
        
        final String maskName = args[2].toLowerCase();
        final MaskType eventMaskType = this.parseMaskType(maskName);
        
        if (eventMaskType == null) {
            sender.sendMessage(Component.text("Invalid mask type! Available: forbidden_shadows, primordial_flame, void, lightning", NamedTextColor.RED));
            return true;
        }
        
        // Double confirmation system
        final UUID senderId = sender instanceof Player ? ((Player) sender).getUniqueId() : new UUID(0, 0);
        final String commandKey = "give_" + targetPlayer.getName() + "_" + maskName;
        
        if (this.requiresConfirmation(senderId, commandKey)) {
            sender.sendMessage(Component.text("⚠ Warning: You are about to give " + targetPlayer.getName() + " the Event Mask.", NamedTextColor.RED));
            sender.sendMessage(Component.text("Run /event give " + targetPlayer.getName() + " " + maskName + " again within 15 seconds to confirm.", NamedTextColor.YELLOW));
            return true;
        }
        
        // Execute the command
        this.plugin.getMaskManager().assignEventMask(targetPlayer, eventMaskType);
        
        // Send confirmation messages
        sender.sendMessage(Component.text("[Admin] You have given " + targetPlayer.getName() + " the Event Mask.", NamedTextColor.GREEN));
        
        final Component targetMessage = this.getEventMaskGrantMessage(eventMaskType);
        targetPlayer.sendMessage(targetMessage);
        
        // Broadcast to server
        final Component broadcastMessage = Component.text()
                .append(Component.text("⚡ [EVENT] ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
                .append(Component.text(targetPlayer.getName(), NamedTextColor.YELLOW))
                .append(Component.text(" has been granted the ", NamedTextColor.GRAY))
                .append(eventMaskType.getFormattedName())
                .append(Component.text("!", NamedTextColor.GRAY))
                .build();
        
        this.plugin.getServer().broadcast(broadcastMessage);
        
        return true;
    }
    
    private boolean handleRemove(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission("oni.admin.event")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /event remove <player>", NamedTextColor.RED));
            return true;
        }
        
        final Player targetPlayer = this.plugin.getServer().getPlayer(args[1]);
        if (targetPlayer == null) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }
        
        final PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(targetPlayer.getUniqueId());
        if (!playerData.getMaskType().isEventMask()) {
            sender.sendMessage(Component.text("Player does not have an Event Mask!", NamedTextColor.RED));
            return true;
        }
        
        final MaskType currentEventMask = playerData.getMaskType();
        
        // Remove event mask and assign random normal mask
        this.plugin.getMaskManager().assignRandomMask(targetPlayer);
        
        // Send messages
        sender.sendMessage(Component.text("[Admin] You have forcibly removed the Event Mask from " + targetPlayer.getName(), NamedTextColor.GREEN));
        
        final Component targetMessage = this.getEventMaskRemovedMessage(currentEventMask);
        targetPlayer.sendMessage(targetMessage);
        
        return true;
    }
    
    private boolean handleTransfer(final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }
        
        final Player player = (Player) sender;
        final PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        
        if (!playerData.getMaskType().isEventMask()) {
            player.sendMessage(Component.text("You don't have an Event Mask to transfer!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /event transfer <player>", NamedTextColor.RED));
            return true;
        }
        
        final Player targetPlayer = this.plugin.getServer().getPlayer(args[1]);
        if (targetPlayer == null) {
            player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }
        
        if (targetPlayer.equals(player)) {
            player.sendMessage(Component.text("You cannot transfer to yourself!", NamedTextColor.RED));
            return true;
        }
        
        // Check if target is trusted
        if (!playerData.isTrusted(targetPlayer.getUniqueId())) {
            player.sendMessage(Component.text("You can only transfer to trusted players!", NamedTextColor.RED));
            return true;
        }
        
        final MaskType eventMaskType = playerData.getMaskType();
        
        // Transfer the mask
        this.plugin.getMaskManager().assignEventMask(targetPlayer, eventMaskType);
        this.plugin.getMaskManager().assignRandomMask(player);
        
        // Send transfer messages
        final Component senderMessage = this.getTransferSenderMessage(eventMaskType, targetPlayer.getName());
        final Component receiverMessage = this.getTransferReceiverMessage(eventMaskType, player.getName());
        
        player.sendMessage(senderMessage);
        targetPlayer.sendMessage(receiverMessage);
        
        return true;
    }
    
    private boolean handleDelete(final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }
        
        final Player player = (Player) sender;
        final PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        
        if (!playerData.getMaskType().isEventMask()) {
            player.sendMessage(Component.text("You don't have an Event Mask to delete!", NamedTextColor.RED));
            return true;
        }
        
        // Double confirmation system
        final String commandKey = "delete_" + player.getName();
        
        if (this.requiresConfirmation(player.getUniqueId(), commandKey)) {
            player.sendMessage(Component.text("⚠ Warning: You are about to delete your Event Mask.", NamedTextColor.RED));
            player.sendMessage(Component.text("Run /event delete again within 15 seconds to confirm.", NamedTextColor.YELLOW));
            return true;
        }
        
        final MaskType eventMaskType = playerData.getMaskType();
        
        // Delete event mask and assign random normal mask
        this.plugin.getMaskManager().assignRandomMask(player);
        
        // Send deletion message
        final Component deletionMessage = this.getEventMaskDeletedMessage(eventMaskType);
        player.sendMessage(deletionMessage);
        
        // Broadcast abandonment
        final Component broadcastMessage = Component.text()
                .append(Component.text("[Event] ", NamedTextColor.DARK_GRAY))
                .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                .append(Component.text(" has abandoned the " + eventMaskType.getDisplayName(), NamedTextColor.GRAY))
                .build();
        
        this.plugin.getServer().broadcast(broadcastMessage);
        
        return true;
    }
    
    private boolean requiresConfirmation(final UUID playerId, final String commandKey) {
        final long currentTime = System.currentTimeMillis();
        final Long lastConfirmation = this.confirmationTimestamps.get(playerId);
        final String lastCommand = this.pendingCommands.get(playerId);
        
        if (lastConfirmation != null && lastCommand != null && 
            lastCommand.equals(commandKey) && 
            currentTime - lastConfirmation < 15000) {
            // Confirmation valid, clear it
            this.confirmationTimestamps.remove(playerId);
            this.pendingCommands.remove(playerId);
            return false;
        } else {
            // Set confirmation
            this.confirmationTimestamps.put(playerId, currentTime);
            this.pendingCommands.put(playerId, commandKey);
            return true;
        }
    }
    
    private MaskType parseMaskType(final String maskName) {
        return switch (maskName) {
            case "forbidden_shadows" -> MaskType.FORBIDDEN_SHADOWS;
            case "primordial_flame" -> MaskType.PRIMORDIAL_FLAME;
            case "void" -> MaskType.VOID;
            case "lightning" -> MaskType.LIGHTNING;
            default -> null;
        };
    }
    
    private Component getEventMaskGrantMessage(final MaskType maskType) {
        return switch (maskType) {
            case FORBIDDEN_SHADOWS -> Component.text("§5[Forbidden Shadows] §7The Forbidden Mask has been bestowed upon you by an administrator.");
            case PRIMORDIAL_FLAME -> Component.text("§6[Primordial Flame] §7The Primordial Mask has been bestowed upon you by an administrator.");
            case VOID -> Component.text("§8[Void] §7The Void Mask has been bestowed upon you by an administrator.");
            case LIGHTNING -> Component.text("§e[Lightning] §7The Lightning Mask has been bestowed upon you by an administrator.");
            default -> Component.text("§7You have been granted an Event Mask.");
        };
    }
    
    private Component getEventMaskRemovedMessage(final MaskType maskType) {
        return switch (maskType) {
            case FORBIDDEN_SHADOWS -> Component.text("§5[Forbidden Shadows] §7Your Event Mask has been stripped by an administrator. §7A new mask has been assigned to you.");
            case PRIMORDIAL_FLAME -> Component.text("§6[Primordial Flame] §7Your Event Mask has been stripped by an administrator. §7A new mask has been assigned to you.");
            case VOID -> Component.text("§8[Void] §7Your Event Mask has been stripped by an administrator. §7A new mask has been assigned to you.");
            case LIGHTNING -> Component.text("§e[Lightning] §7Your Event Mask has been stripped by an administrator. §7A new mask has been assigned to you.");
            default -> Component.text("§7Your Event Mask has been removed.");
        };
    }
    
    private Component getTransferSenderMessage(final MaskType maskType, final String targetName) {
        return switch (maskType) {
            case FORBIDDEN_SHADOWS -> Component.text("§5[Forbidden Shadows] §7You have passed your Event Mask to §e" + targetName + "§7. §7The darkness leaves you... another takes your place.");
            case PRIMORDIAL_FLAME -> Component.text("§6[Primordial Flame] §7You have entrusted your fiery legacy to §e" + targetName + "§7. §7The inferno abandons you, roaring into their soul.");
            case VOID -> Component.text("§8[Void] §7You have transferred your void powers to §e" + targetName + "§7. §7The abyss shifts its allegiance.");
            case LIGHTNING -> Component.text("§e[Lightning] §7You have passed the storm to §e" + targetName + "§7. §7Thunder follows a new master.");
            default -> Component.text("§7You have transferred your Event Mask to §e" + targetName + "§7.");
        };
    }
    
    private Component getTransferReceiverMessage(final MaskType maskType, final String senderName) {
        return switch (maskType) {
            case FORBIDDEN_SHADOWS -> Component.text("§5[Forbidden Shadows] §e" + senderName + " §7has entrusted you with the Event Mask. §7The shadows coil around you, embracing your soul.");
            case PRIMORDIAL_FLAME -> Component.text("§6[Primordial Flame] §e" + senderName + " §7has granted you the Event Mask. §7Flames surge into your veins, burning brighter than ever.");
            case VOID -> Component.text("§8[Void] §e" + senderName + " §7has transferred the void to you. §7The abyss whispers your name.");
            case LIGHTNING -> Component.text("§e[Lightning] §e" + senderName + " §7has passed the storm to you. §7Lightning crackles in your soul.");
            default -> Component.text("§e" + senderName + " §7has transferred an Event Mask to you.");
        };
    }
    
    private Component getEventMaskDeletedMessage(final MaskType maskType) {
        return switch (maskType) {
            case FORBIDDEN_SHADOWS -> Component.text("§5[Forbidden Shadows] §7Your Event Mask has been destroyed. §7The shadows disperse, leaving you with a normal mask.");
            case PRIMORDIAL_FLAME -> Component.text("§6[Primordial Flame] §7Your Event Mask has been extinguished. §7The fire fades, leaving you with a normal mask.");
            case VOID -> Component.text("§8[Void] §7Your Event Mask has been consumed by the void. §7You return to normalcy.");
            case LIGHTNING -> Component.text("§e[Lightning] §7Your Event Mask has been discharged. §7The storm moves on, leaving you with a normal mask.");
            default -> Component.text("§7Your Event Mask has been destroyed.");
        };
    }
    
    private void sendUsage(final CommandSender sender) {
        sender.sendMessage(Component.text("Event Mask Commands:", NamedTextColor.GOLD));
        
        if (sender.hasPermission("oni.admin.event")) {
            sender.sendMessage(Component.text("/event give <player> <mask> - Grant an event mask", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("/event remove <player> - Remove an event mask", NamedTextColor.YELLOW));
        }
        
        if (sender instanceof Player) {
            sender.sendMessage(Component.text("/event transfer <player> - Transfer your event mask", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("/event delete - Delete your event mask", NamedTextColor.YELLOW));
        }
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, 
                                               @NotNull final String alias, @NotNull final String[] args) {
        
        final List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            if (sender.hasPermission("oni.admin.event")) {
                completions.add("give");
                completions.add("remove");
            }
            if (sender instanceof Player) {
                completions.add("transfer");
                completions.add("delete");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("transfer")) {
                return this.plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .toList();
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            completions.add("forbidden_shadows");
            completions.add("primordial_flame");
            completions.add("void");
            completions.add("lightning");
        }
        
        return completions;
    }
}