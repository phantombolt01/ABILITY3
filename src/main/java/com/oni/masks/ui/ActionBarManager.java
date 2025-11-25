package com.oni.masks.ui;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.masks.Mask;
import com.oni.masks.player.PlayerData;
import com.oni.masks.player.PlayerDataManager;
import com.oni.masks.masks.MaskType;
import com.oni.masks.sins.Sin;
import com.oni.masks.sins.SinType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarManager {

    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
    private BukkitRunnable actionBarTask;

    public ActionBarManager() {
        this.startActionBarUpdates();
    }

    private void startActionBarUpdates() {
        this.actionBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (final Player player : plugin.getServer().getOnlinePlayers()) {
                    updatePlayerActionBar(player);
                }
            }
        };
        this.actionBarTask.runTaskTimer(this.plugin, 0L, 1L); // Update every tick
    }

    private void updatePlayerActionBar(final Player player) {
        final PlayerData playerData = this.playerDataManager.getPlayerData(player.getUniqueId());
        final Mask mask = playerData.getCurrentMask();
        final Sin sin = playerData.getCurrentSin();

        if (mask == null && sin == null) {
            return;
        }

        // Prioritize sin display over mask display
        if (sin != null) {
            final Component sinActionBar = this.buildSinActionBarMessage(player, sin, playerData);
            player.sendActionBar(sinActionBar);
            return;
        }

        // Check if player has an event mask
        if (playerData.getMaskType().isEventMask()) {
            final Component eventActionBar = this.buildEventActionBarMessage(player, playerData);
            player.sendActionBar(eventActionBar);
            return;
        }

        // Default mask visuals (classic version)
        final Component actionBarMessage = this.buildActionBarMessage(player, mask, playerData);
        player.sendActionBar(actionBarMessage);
    }

    private Component buildSinActionBarMessage(final Player player, final Sin sin, final PlayerData playerData) {
        Component message = Component.empty();

        // Left: Sin icon + name
        final SinType sinType = sin.getSinType();
        message = message.append(Component.text(sinType.getActionBarIcon() + " ", sinType.getColor()))
                .append(Component.text(sinType.getDisplayName(), sinType.getColor()))
                .append(Component.text(" │ ", NamedTextColor.DARK_GRAY));

        // Add ability status
        final var abilities = sin.getAbilities();
        for (int i = 0; i < abilities.size(); i++) {
            if (i > 0) {
                message = message.append(Component.text(" │ ", NamedTextColor.DARK_GRAY)); // separator same style
            }

            final var ability = abilities.get(i);
            final String abilityName = ability.getName();
            final long remainingCooldown = playerData.getRemainingCooldown(ability.getName());

            if (remainingCooldown > 0) {
                final double remainingSeconds = remainingCooldown / 1000.0;
                final String cooldownBar = this.createCooldownBar(remainingSeconds, ability.getDefaultCooldownSeconds());
                message = message.append(Component.text(abilityName + " ", NamedTextColor.GRAY))
                        .append(Component.text(cooldownBar + " ", NamedTextColor.YELLOW))
                        .append(Component.text(String.format("%.1fs", remainingSeconds), NamedTextColor.YELLOW));
            } else {
                message = message.append(Component.text(abilityName + " ", NamedTextColor.GRAY))
                        .append(Component.text("● ", NamedTextColor.GREEN))
                        .append(Component.text("READY", NamedTextColor.GREEN));
            }
        }

        // Right: Hearts indicator
        message = message.append(Component.text(" │ ", NamedTextColor.DARK_GRAY))
                .append(Component.text("♥" + sinType.getHearts(), NamedTextColor.RED));

        return message;
    }

    private Component buildActionBarMessage(final Player player, final Mask mask, final PlayerData playerData) {
        Component message = Component.empty();

        // Left: Mask icon + name
        final MaskType maskType = mask.getMaskType();
        message = message.append(Component.text(maskType.getActionBarIcon() + " ", maskType.getColor()))
                .append(Component.text(maskType.getDisplayName(), maskType.getColor()))
                .append(Component.text(" │ ", NamedTextColor.DARK_GRAY));

        // Add ability status (classic style visuals)
        final var abilities = mask.getAbilities();
        for (int i = 0; i < abilities.size(); i++) {
            if (i > 0) {
                message = message.append(Component.text(" || ", NamedTextColor.DARK_GRAY)); // original separator
            }

            final var ability = abilities.get(i);
            final String abilityName = ability.getName();
            final long remainingCooldown = playerData.getRemainingCooldown(ability.getName());

            if (remainingCooldown > 0) {
                final double remainingSeconds = remainingCooldown / 1000.0;
                final String cooldownBar = this.createCooldownBar(remainingSeconds, ability.getDefaultCooldownSeconds());
                message = message.append(Component.text(abilityName + " ", NamedTextColor.GRAY))
                        .append(Component.text(cooldownBar + " ", NamedTextColor.YELLOW))
                        .append(Component.text(String.format("%.1fs", remainingSeconds), NamedTextColor.YELLOW));
            } else {
                message = message.append(Component.text(abilityName + " ", NamedTextColor.GRAY))
                        .append(Component.text("● ", NamedTextColor.GREEN))
                        .append(Component.text("READY", NamedTextColor.GREEN));
            }
        }

        // Right: Upgrade level indicator
        final int tier = playerData.getTierLevel();
        message = message.append(Component.text(" │ ", NamedTextColor.DARK_GRAY));

        for (int i = 0; i < 2; i++) {
            if (i < tier) {
                message = message.append(Component.text("★", NamedTextColor.GOLD));
            } else {
                message = message.append(Component.text("☆", NamedTextColor.GRAY));
            }
        }

        return message;
    }

    private Component buildEventActionBarMessage(final Player player, final PlayerData playerData) {
        Component message = Component.empty();

        final MaskType maskType = playerData.getMaskType();

        // Left: Event mask icon + name
        message = message.append(Component.text(maskType.getActionBarIcon() + " ", maskType.getColor()))
                .append(Component.text(maskType.getDisplayName(), maskType.getColor()))
                .append(Component.text(" │ ", NamedTextColor.DARK_GRAY));

        // Center: Event ability cooldown (get first ability name)
        final var abilities = playerData.getCurrentMask().getAbilities();
        final String abilityName = abilities.isEmpty() ? "Event Ability" : abilities.get(0).getName();
        final long remainingCooldown = playerData.getRemainingCooldown(abilityName);

        if (remainingCooldown > 0) {
            final double remainingSeconds = remainingCooldown / 1000.0;
            final int maxCooldown = abilities.isEmpty() ? 15 : abilities.get(0).getDefaultCooldownSeconds();
            final String cooldownBar = this.createCooldownBar(remainingSeconds, maxCooldown);
            message = message.append(Component.text(cooldownBar + " ", NamedTextColor.YELLOW))
                    .append(Component.text(String.format("%.1fs", remainingSeconds), NamedTextColor.YELLOW));
        } else {
            message = message.append(Component.text("● ", NamedTextColor.GREEN))
                    .append(Component.text("READY", NamedTextColor.GREEN));
        }

        // Right: Event-specific info
        if (maskType == MaskType.FORBIDDEN_SHADOWS) {
            final int currentStage = playerData.getCurrentEventStage();
            final int stageXP = playerData.getEventStageXP();
            final String stageName = this.getStageName(currentStage);

            message = message.append(Component.text(" │ ", NamedTextColor.DARK_GRAY))
                    .append(Component.text("Stage " + currentStage + " — " + stageName, NamedTextColor.RED))
                    .append(Component.text(" [XP: " + stageXP + "]", NamedTextColor.GRAY));
        } else if (maskType == MaskType.PRIMORDIAL_FLAME || maskType == MaskType.VOID ||
                maskType == MaskType.LIGHTNING ) {
            final int tier = playerData.getTierLevel();
            message = message.append(Component.text(" │ ", NamedTextColor.DARK_GRAY));
            for (int i = 0; i < 2; i++) {
                if (i < tier) {
                    message = message.append(Component.text("★", NamedTextColor.GOLD));
                } else {
                    message = message.append(Component.text("☆", NamedTextColor.GRAY));
                }
            }
        }

        return message;
    }

    private String getStageName(final int stage) {
        return switch (stage) {
            case 1 -> "Hounds of the Abyss";
            case 2 -> "Black Parrot Swarm";
            case 3 -> "Killer Rabbits";
            case 4 -> "Berserk Panda";
            case 5 -> "Shadow Warden";
            default -> "Unknown";
        };
    }

    private String createCooldownBar(final double remainingSeconds, final int maxSeconds) {
        final double progress = 1.0 - (remainingSeconds / maxSeconds);
        final int segments = 8;
        final int filledSegments = (int) (progress * segments);

        final StringBuilder bar = new StringBuilder();
        for (int i = 0; i < segments; i++) {
            if (i < filledSegments) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        return bar.toString();
    }

    public void shutdown() {
        if (this.actionBarTask != null) {
            this.actionBarTask.cancel();
        }
    }
}
