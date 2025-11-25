package com.oni.masks.masks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

@Getter
@RequiredArgsConstructor
public enum MaskType {
    
    FLAME("Flame Mask", TextColor.fromHexString("#FF4500"), "🔥"),
    EARTH("Earth Mask", TextColor.fromHexString("#8B4513"), "🪨"),
    WATER("Water Mask", TextColor.fromHexString("#0080FF"), "🌊"),
    LIGHT("Mask of Light", TextColor.fromHexString("#FFD700"), "🌟"),
    VOID("Mask of the Void", TextColor.fromHexString("#1A0033"), "☠"),
    LIGHTNING("Mask of Lightning", TextColor.fromHexString("#FFFF00"), "⚡"),
    
    // Event Masks - Admin-only, no crafting
    FORBIDDEN_SHADOWS("Mask of Forbidden Shadows", TextColor.fromHexString("#2C1810"), "🌑"),
    PRIMORDIAL_FLAME("Mask of the Primordial Flame", TextColor.fromHexString("#FF6600"), "🔥");
    
    private final String displayName;
    private final TextColor color;
    private final String emoji;
    
    public boolean isEventMask() {
        return this == FORBIDDEN_SHADOWS || this == PRIMORDIAL_FLAME;
    }
    
    public Component getFormattedName() {
        return Component.text(this.displayName)
                .color(this.color)
                .append(Component.text(" " + this.emoji));
    }
    
    public String getActionBarIcon() {
        return switch (this) {
            case FORBIDDEN_SHADOWS -> "㍴";
            case LIGHT -> "㌀";
            case WATER -> "㍻";
            case FLAME -> "㍬";
            case EARTH -> "㋏";
            case VOID -> "㌆";
            case LIGHTNING -> "㌇";
            case PRIMORDIAL_FLAME -> "㍿";
        };
    }
    
    public Component getAnnouncementMessage() {
        if (this.isEventMask()) {
            return Component.text("You have been granted the legendary ", NamedTextColor.DARK_PURPLE)
                    .append(this.getFormattedName())
                    .append(Component.text("!", NamedTextColor.DARK_PURPLE));
        } else {
            return Component.text("You have been blessed with the ", NamedTextColor.GOLD)
                    .append(this.getFormattedName())
                    .append(Component.text("!", NamedTextColor.GOLD));
        }
    }
}