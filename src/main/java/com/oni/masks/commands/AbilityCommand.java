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
        final Sin sin = playerData.getCurrentSin();

        if (mask == null) {
            player.sendMessage(Component.text("You don't have a mask equipped!", NamedTextColor.RED));
            return true;
        }

        final String commandName = command.getName().toLowerCase();

        if (commandName.equals("ability1")) {
            if (mask.getAbilities().isEmpty()) {
                player.sendMessage(Component.text("Your mask doesn't have a first ability!", NamedTextColor.RED));
                return true;
            }

            final var ability1 = mask.getAbilities().get(0);
            ability1.use();
            playerData.applyTriangleCooldown(ability1.getName());

        } else if (commandName.equals("ability2")) {
            if (mask.getAbilities().size() < 2) {
                player.sendMessage(Component.text("Your mask doesn't have a second ability!", NamedTextColor.RED));
                return true;
            }

            final var ability2 = mask.getAbilities().get(1);
            ability2.use();
            playerData.applyTriangleCooldown(ability2.getName());

        } else if (commandName.equals("ability3")) {
            if (!playerData.isHasSinItem() || sin == null) {
                player.sendMessage(Component.text("You do not wield a Sin. Commit crimes to earn one.", NamedTextColor.RED));
                return true;
            }

            if (sin.getAbilities().isEmpty()) {
                player.sendMessage(Component.text("Your sin doesn't have an ability!", NamedTextColor.RED));
                return true;
            }

            final var ability3 = sin.getAbilities().get(0);
            ability3.use();
            playerData.applyTriangleCooldown(ability3.getName());
        }

        return true;
    }
}
