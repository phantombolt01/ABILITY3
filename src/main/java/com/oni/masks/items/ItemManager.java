// ADD TO PUBLIC
package com.oni.masks.items;

import com.oni.masks.OniMasksPlugin;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ItemManager {

    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final NamespacedKey rerollItemKey = new NamespacedKey(this.plugin, "reroll_item");

    public ItemManager() {
        this.registerRecipes();
        }
    
    private void registerRecipes() {
        // Only register reroll item recipe - event masks are admin-only
        if (this.plugin.getConfigManager().getPluginConfig().isRerollItemRecipeEnabled()) {
            this.registerRerollItemRecipe();
        }
        }

    public ItemStack createRerollItem() {
        final ItemStack item = new ItemStack(Material.CLOCK);
        final ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Mask Reroll Token", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(List.of(
                Component.text("Reforge your destiny!", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Evil laugh. Ha ha ha.", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Right-click to reroll your mask", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.getPersistentDataContainer().set(this.rerollItemKey, PersistentDataType.BOOLEAN, true);
        meta.setCustomModelData(331);
        
        item.setItemMeta(meta);
        return item;
        }

    public boolean isRerollItem(final ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
        return false;
        }
        
        return item.getItemMeta().getPersistentDataContainer()
                .has(this.rerollItemKey, PersistentDataType.BOOLEAN);
}

private void registerRerollItemRecipe() {
ItemStack rerollItem = this.createRerollItem();
        NamespacedKey key = new NamespacedKey(this.plugin, "reroll_item_recipe");

        ShapedRecipe recipe = new ShapedRecipe(key, rerollItem);
        recipe.shape("GDG", "RER", "GDG");

        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        recipe.setIngredient('E', Material.ENDER_EYE);

        this.plugin.getServer().addRecipe(recipe);
}
public boolean isEventMask(final ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        final ItemMeta meta = item.getItemMeta();
        if (meta.displayName() == null) return false;

        final String name = LegacyComponentSerializer.legacySection().serialize(meta.displayName());
        return name.contains("Forbidden Shadows") || name.contains("Primordial Flame");
        }
} 