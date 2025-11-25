package com.oni.masks.player;

import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * OniMasks Damage System (1.21 compatible)
 *
 * Deals consistent visible damage (e.g. 3 hearts) regardless of armor or enchantments.
 * Still respects totems, resistance, and all vanilla behaviors.
 */
public class DamageUtils {

    /**
     * Apply adaptive damage that results in the same number of hearts lost
     * regardless of armor or enchantments (1 heart = 2.0 HP).
     *
     * Works fully in Minecraft 1.21+.
     */
    public static void applyAdaptiveDamage(LivingEntity target, double desiredHearts, Player attacker) {
        if (target == null || desiredHearts <= 0) return;

        double desiredDamage = desiredHearts * 2.0;

        // --- Step 1: Gather defense stats ---
        double armor = getAttribute(target, Attribute.GENERIC_ARMOR);
        double toughness = getAttribute(target, Attribute.GENERIC_ARMOR_TOUGHNESS);
        int protLevel = getTotalProtectionLevel(target);

        // --- Step 2: Compute vanilla reduction factors (1.21 accurate) ---
        double armorFactor = getArmorReductionFactor(armor, toughness, desiredDamage);
        double protFactor = getProtectionReductionFactor(protLevel);

        // Total final reduction applied by vanilla (multiplicative)
        double totalReduction = armorFactor * protFactor;

        // Clamp to safe range
        totalReduction = Math.max(0.05, Math.min(1.0, totalReduction));

        // --- Step 3: Reverse it to deal the intended visible damage ---
        double rawDamage = desiredDamage / totalReduction;

        // --- Step 4: Cap extreme values (safety) ---
        double maxHealth = getAttribute(target, Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth <= 0) maxHealth = 20.0;
        rawDamage = Math.min(rawDamage, maxHealth * 4.0);

        // --- Step 5: Apply normally (totems still work) ---
        if (attacker != null)
            target.damage(rawDamage, attacker);
        else
            target.damage(rawDamage);
    }

    // --- Utils ---

    private static double getAttribute(LivingEntity e, Attribute attr) {
        return e.getAttribute(attr) != null ? e.getAttribute(attr).getValue() : 0.0;
    }

    private static int getTotalProtectionLevel(LivingEntity entity) {
        if (!(entity instanceof Player)) return 0;
        Player player = (Player) entity;
        int total = 0;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.containsEnchantment(Enchantment.PROTECTION)) {
                total += item.getEnchantmentLevel(Enchantment.PROTECTION);
            }
        }
        return total;
    }

    /**
     * 1.21 Armor reduction formula (same as legacy, uses doubles)
     */
    private static double getArmorReductionFactor(double armor, double toughness, double incomingDamage) {
        if (armor <= 0) return 1.0;
        double armorReduction = Math.min(20.0,
                Math.max(armor / 5.0, armor - incomingDamage / (2.0 + toughness / 4.0))
        ) / 25.0;
        return 1.0 - armorReduction;
    }

    /**
     * 1.21 Protection formula — multiplicative 4% per level.
     * Total reduction = 1 - 0.04 per level, compounded.
     */
    private static double getProtectionReductionFactor(int protLevel) {
        if (protLevel <= 0) return 1.0;
        return Math.pow(0.96, protLevel); // multiplicative 4% per level
    }
}
