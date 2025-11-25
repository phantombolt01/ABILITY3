package com.oni.masks.ui;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.config.ConfigManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ConfigGUI implements Listener {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final ConfigManager configManager = this.plugin.getConfigManager();
    private final Map<UUID, String> playerConfigSessions = new HashMap<>();
    
    public void openGUI(final Player player) {
        final Inventory gui = this.plugin.getServer().createInventory(
                null, 54, Component.text("Oni Masks Configuration", NamedTextColor.DARK_PURPLE)
        );
        
        this.setupGUIItems(gui);
        player.openInventory(gui);
        
        // Register this as a temporary listener
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }
    
    private void setupGUIItems(final Inventory gui) {
        // Cooldown settings section
        gui.setItem(10, this.createConfigItem(Material.CLOCK, "Cooldown Settings", 
                "Click to adjust ability cooldowns", "cooldowns"));
        
        // Damage settings section
        gui.setItem(12, this.createConfigItem(Material.DIAMOND_SWORD, "Damage Settings", 
                "Click to adjust ability damage values", "damage"));
        
        // Particle settings section
        gui.setItem(14, this.createConfigItem(Material.BLAZE_POWDER, "Particle Settings", 
                "Click to adjust particle effects", "particles"));
        
        // Event mask settings section
        gui.setItem(16, this.createConfigItem(Material.ECHO_SHARD, "Event Mask Settings", 
                "Click to configure event mask behavior", "event-mask"));
        
        // Recipe settings section
        gui.setItem(28, this.createConfigItem(Material.CRAFTING_TABLE, "Recipe Settings", 
                "Click to toggle recipe availability", "recipes"));
        
        // Save and reload button
        gui.setItem(40, this.createActionItem(Material.EMERALD, "Save & Reload", 
                "Click to save changes and reload configuration"));
        
        // Close button
        gui.setItem(44, this.createActionItem(Material.BARRIER, "Close", 
                "Click to close this menu"));
    }
    
    private ItemStack createConfigItem(final Material material, final String name, 
                                     final String description, final String configSection) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text(name, NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(List.of(
                Component.text(description, NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Section: " + configSection, NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createActionItem(final Material material, final String name, 
                                     final String description) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text(name, NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(List.of(
                Component.text(description, NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        final Player player = (Player) event.getWhoClicked();
        final Component title = event.getView().title();
        
        if (!title.equals(Component.text("Oni Masks Configuration", NamedTextColor.DARK_PURPLE))) {
            return;
        }
        
        event.setCancelled(true);
        
        final ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }
        
        final Component itemName = clickedItem.getItemMeta().displayName();
        
        if (itemName == null) {
            return;
        }
        
        // Handle different configuration sections
        if (itemName.equals(Component.text("Cooldown Settings", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false))) {
            this.openCooldownSettings(player);
        } else if (itemName.equals(Component.text("Damage Settings", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false))) {
            this.openDamageSettings(player);
        } else if (itemName.equals(Component.text("Save & Reload", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false))) {
            this.saveAndReload(player);
        } else if (itemName.equals(Component.text("Close", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false))) {
            player.closeInventory();
        }
    }
    
    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        final Player player = (Player) event.getPlayer();
        final Component title = event.getView().title();
        
        if (title.equals(Component.text("Oni Masks Configuration", NamedTextColor.DARK_PURPLE))) {
            this.playerConfigSessions.remove(player.getUniqueId());
        }
    }
    
    private void openCooldownSettings(final Player player) {
        // In a full implementation, this would open a sub-GUI for cooldown settings
        player.sendMessage(Component.text("Cooldown settings would open here. Use commands for now:", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Current default cooldown: " + 
                this.configManager.getPluginConfig().getDefaultCooldown() + " seconds", NamedTextColor.GRAY));
    }
    
    private void openDamageSettings(final Player player) {
        // In a full implementation, this would open a sub-GUI for damage settings
        player.sendMessage(Component.text("Damage settings would open here. Use commands for now:", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Current fireball damage: " + 
                this.configManager.getPluginConfig().getFireballDamage(), NamedTextColor.GRAY));
    }
    
    private void saveAndReload(final Player player) {
        this.configManager.saveConfig();
        this.configManager.reloadConfig();
        
        player.sendMessage(Component.text("Configuration saved and reloaded!", NamedTextColor.GREEN));
        player.closeInventory();
    }
}