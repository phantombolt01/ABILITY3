package com.oni.masks.player;

import com.oni.masks.OniMasksPlugin;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

@RequiredArgsConstructor
public class TrustManager {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
    
    public void trustPlayer(final Player trustor, final Player target) {
        if (trustor.equals(target)) {
            trustor.sendMessage(Component.text("You cannot trust yourself!", NamedTextColor.RED));
            return;
        }
        
        final PlayerData trustorData = this.playerDataManager.getPlayerData(trustor.getUniqueId());
        final UUID targetId = target.getUniqueId();
        
        if (trustorData.isTrusted(targetId)) {
            trustor.sendMessage(Component.text("You already trust " + target.getName() + "!", NamedTextColor.YELLOW));
            return;
        }
        
        trustorData.trustPlayer(targetId);
        this.playerDataManager.savePlayerData(trustor.getUniqueId());
        
        trustor.sendMessage(Component.text("You now trust " + target.getName() + ".", NamedTextColor.GREEN));
        target.sendMessage(Component.text("You have been trusted by " + trustor.getName() + ".", NamedTextColor.GREEN));
        
        this.plugin.getSoundManager().playTrustSound(trustor);
        this.plugin.getSoundManager().playTrustSound(target);
    }
    
    public void untrustPlayer(final Player trustor, final Player target) {
        if (trustor.equals(target)) {
            trustor.sendMessage(Component.text("You cannot untrust yourself!", NamedTextColor.RED));
            return;
        }
        
        final PlayerData trustorData = this.playerDataManager.getPlayerData(trustor.getUniqueId());
        final UUID targetId = target.getUniqueId();
        
        if (!trustorData.isTrusted(targetId)) {
            trustor.sendMessage(Component.text("You don't trust " + target.getName() + "!", NamedTextColor.YELLOW));
            return;
        }
        
        trustorData.untrustPlayer(targetId);
        this.playerDataManager.savePlayerData(trustor.getUniqueId());
        
        trustor.sendMessage(Component.text("You have untrusted " + target.getName() + ".", NamedTextColor.RED));
        target.sendMessage(Component.text(trustor.getName() + " has untrusted you.", NamedTextColor.DARK_RED));
        
        this.plugin.getSoundManager().playUntrustSound(trustor);
        this.plugin.getSoundManager().playUntrustSound(target);
    }
    
    public boolean canHarm(final Player attacker, final Player target) {
        final PlayerData attackerData = this.playerDataManager.getPlayerData(attacker.getUniqueId());
        return !attackerData.isTrusted(target.getUniqueId());
    }
    
    public boolean canHelp(final Player helper, final Player target) {
        final PlayerData helperData = this.playerDataManager.getPlayerData(helper.getUniqueId());
        return helperData.isTrusted(target.getUniqueId());
    }

    // ✅ for normal use
    public Player findNearestUntrustedPlayer(final Player player, final double radius) {
        Player nearest = null;
        double closest = Double.MAX_VALUE;

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(player)) continue;
            if (!this.canHarm(player, target)) continue;

            double distance = player.getLocation().distance(target.getLocation());
            if (distance < radius && distance < closest) {
                closest = distance;
                nearest = target;
            }
        }
        return nearest;
    }

    // ✅ overload for your MobProgressionManager (uses Location)
    public Player findNearestUntrustedPlayer(final Player player, final Location center) {
        return findNearestUntrustedPlayer(player, center, 20); // default radius 20
    }

    public Player findNearestUntrustedPlayer(final Player player, final Location center, final double radius) {
        Player nearest = null;
        double closest = Double.MAX_VALUE;

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(player)) continue;
            if (!this.canHarm(player, target)) continue;

            double distance = center.distance(target.getLocation());
            if (distance < radius && distance < closest) {
                closest = distance;
                nearest = target;
            }
        }
        return nearest;
    }
}
