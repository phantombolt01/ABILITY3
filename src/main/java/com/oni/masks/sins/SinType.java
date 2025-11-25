package com.oni.masks.sins;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

@Getter
@RequiredArgsConstructor
public enum SinType {
    
    PRIDE("Pride - The Fallen King", TextColor.fromHexString("#800080"), "👑", 15),
    WRATH("Wrath - The Infernal Storm", TextColor.fromHexString("#FF0000"), "🔥", 5),
    ENVY("Envy - The Green Hunger", TextColor.fromHexString("#00FF00"), "💚", 15),
    GREED("Greed - The Collector", TextColor.fromHexString("#FFD700"), "💰", 15),
    LUST("Lust - The Crimson Desire", TextColor.fromHexString("#FF69B4"), "💗", 15),
    GLUTTONY("Gluttony - The Endless Maw", TextColor.fromHexString("#8B4513"), "🍖", 15),
    SLOTH("Sloth - The Still Abyss", TextColor.fromHexString("#708090"), "💤", 15);
    
    private final String displayName;
    private final TextColor color;
    private final String emoji;
    private final int hearts;
    
    public Component getFormattedName() {
        return Component.text(this.displayName)
                .color(this.color)
                .append(Component.text(" " + this.emoji));
    }
    
    public String getActionBarIcon() {
        return switch (this) {
            case PRIDE -> "👑";
            case WRATH -> "🔥";
            case ENVY -> "💚";
            case GREED -> "💰";
            case LUST -> "💗";
            case GLUTTONY -> "🍖";
            case SLOTH -> "💤";
        };
    }
    
    public Component getAnnouncementMessage() {
        return Component.text("You have been cursed with the Sin of ", NamedTextColor.DARK_RED)
                .append(this.getFormattedName())
                .append(Component.text("!", NamedTextColor.DARK_RED));
    }
}