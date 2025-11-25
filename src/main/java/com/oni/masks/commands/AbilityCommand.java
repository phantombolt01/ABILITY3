package com.oni.masks.commands;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.masks.Mask;
import com.oni.masks.sins.Sin; 
import com.oni.masks.player.PlayerData;
import com.oni.masks.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AbilityCommand implements CommandExecutor {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
    
    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, 
                            @NotNull final String label, @NotNull final String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }
        
        final Player player = (Player) sender;
        final PlayerData playerData = this.playerDataManager.getPlayerData(player.getUniqueId());
        final Mask mask = playerData.getCurrentMask();
        final Sin sin = playerData.getCurrentSin(); // ✅ Added for sins support
        
        // ✅ Added unified check for both
        if (mask == null && sin == null) {
            player.sendMessage(Component.text("You don't have a mask or sin equipped!", NamedTextColor.RED));
            return true;
        }
        
        // ✅ Prioritize sin abilities if player has one equipped
        final var abilities = (sin != null) ? sin.getAbilities() : mask.getAbilities();
        final String type = (sin != null) ? "sin" : "mask";
        
        if (command.getName().equalsIgnoreCase("ability1")) {
            // First ability - equivalent to old Shift + Right Click
            if (!abilities.isEmpty()) {
                abilities.get(0).use();
            } else {
                player.sendMessage(Component.text("Your " + type + " doesn't have a first ability!", NamedTextColor.RED));
            }
        } else if (command.getName().equalsIgnoreCase("ability2")) {
            // Second ability - equivalent to old Shift + Left Click
            if (abilities.size() > 1) {
                abilities.get(1).use();
            } else {
                player.sendMessage(Component.text("Your " + type + " doesn't have a second ability!", NamedTextColor.RED));
            }
        }
        
        return true;
    }
}
