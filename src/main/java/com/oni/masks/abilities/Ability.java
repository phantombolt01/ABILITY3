package com.oni.masks.abilities;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.player.PlayerData;
import com.oni.masks.player.PlayerDataManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

@Getter
@RequiredArgsConstructor
public abstract class Ability {
    
    protected final String name;
    protected final Player player;
    protected final int defaultCooldownSeconds;
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
    
    public abstract void execute();
    
    public boolean canUse() {
        final PlayerData playerData = this.playerDataManager.getPlayerData(this.player.getUniqueId());
        return !playerData.isAbilityOnCooldown(this.name);
    }
    
    public void use() {
        if (!this.canUse()) {
            final long remainingMs = this.getRemainingCooldown();
            final double remainingSeconds = remainingMs / 1000.0;
            this.player.sendMessage(Component.text(
                    String.format("%s is on cooldown for %.1f seconds!", this.name, remainingSeconds),
                    NamedTextColor.RED
            ));
            return;
        }
        
        this.execute();
        this.setCooldown();
    }
    
    private void setCooldown() {
        final PlayerData playerData = this.playerDataManager.getPlayerData(this.player.getUniqueId());
        playerData.setCooldown(this.name, this.getCooldownMs());
    }
    
    public long getRemainingCooldown() {
        final PlayerData playerData = this.playerDataManager.getPlayerData(this.player.getUniqueId());
        return playerData.getRemainingCooldown(this.name);
    }
    
    protected int getCooldownMs() {
        return this.defaultCooldownSeconds * 1000;
    }
    
    protected void sendAbilityMessage(final String message) {
        this.player.sendMessage(Component.text(message, NamedTextColor.AQUA));
    }
}