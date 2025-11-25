package com.oni.masks.abilities.impl;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

public class ForbiddenShadowsAbility extends Ability {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public ForbiddenShadowsAbility(final Player player) {
        super("Summon the Shadows", player, 60);
    }
    
    @Override
    public void execute() {
        final PlayerData playerData = this.plugin.getPlayerDataManager()
                .getPlayerData(this.player.getUniqueId());
        
        // Activate forbidden shadows through mob progression manager
        this.plugin.getEventManager().getMobProgressionManager().activateEventMask(this.player);
        
        // Send feedback message
        final int currentStage = playerData.getCurrentEventStage();
        final String stageName = this.getStageName(currentStage);
        
        final Component message = Component.text()
                .append(Component.text("🌑 ", NamedTextColor.DARK_PURPLE))
                .append(Component.text("§8§l[Forbidden Shadows] ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
                .append(Component.text(stageName + " §7 rise to serve you!", NamedTextColor.GRAY))
                .build();
        
        this.player.sendMessage(message);
        
        // Play dramatic activation effects
        this.plugin.getParticleManager().playEventMaskActivation(this.player);
        this.plugin.getSoundManager().playEventMaskActivation();
    }
    
    private String getStageName(final int stage) {
        return switch (stage) {
            case 1 -> "Hounds of the Abyss";
            case 2 -> "Black Parrot Swarm";
            case 3 -> "Killer Rabbits";
            case 4 -> "Berserk Panda";
            case 5 -> "Shadow Warden";
            default -> "Unknown Shadows";
        };
    }
    
    @Override
    protected int getCooldownMs() {
        return 90000; // 90 seconds cooldown
    }
}