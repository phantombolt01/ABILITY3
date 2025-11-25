package com.oni.masks.sins;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.player.PlayerData;
import com.oni.masks.player.PlayerDataManager;
import com.oni.masks.sins.types.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class SinManager {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
    
    public Sin createSin(final Player player, final SinType sinType) {
        return switch (sinType) {
            case PRIDE -> new PrideSin(player);
            case WRATH -> new WrathSin(player);
            case ENVY -> new EnvySin(player);
            case GREED -> new GreedSin(player);
            case LUST -> new LustSin(player);
            case GLUTTONY -> new GluttonySin(player);
            case SLOTH -> new SlothSin(player);
        };
    }
    
    public void assignSin(final Player player, final SinType sinType) {
        final PlayerData playerData = this.playerDataManager.getPlayerData(player.getUniqueId());

        if (playerData.getCurrentMask() == null) {
            player.sendMessage(Component.text("You must have a mask equipped before wielding a Sin!", NamedTextColor.RED));
            return;
        }

        if (playerData.getCurrentSin() != null) {
            playerData.getCurrentSin().removePassiveEffects();
        }

        final Sin newSin = this.createSin(player, sinType);
        playerData.setCurrentSin(newSin);
        playerData.setSinType(sinType);
        playerData.setHasSinItem(true);

        final double maxHealth = sinType.getHearts() * 2.0;
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            player.setHealth(Math.min(player.getHealth(), maxHealth));
        }

        newSin.applyPassiveEffects();
        newSin.onEquip();

        final Component message = sinType.getAnnouncementMessage();
        player.sendMessage(message);

        this.plugin.getSoundManager().playMaskAssignSound(player);

        this.playerDataManager.savePlayerData(player.getUniqueId());
    }
    
    public void removeSin(final Player player) {
        final PlayerData playerData = this.playerDataManager.getPlayerData(player.getUniqueId());

        if (playerData.getCurrentSin() != null) {
            playerData.getCurrentSin().removePassiveEffects();
            playerData.setCurrentSin(null);
            playerData.setSinType(null);
            playerData.setHasSinItem(false);

            if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
                player.setHealth(Math.min(player.getHealth(), 20.0));
            }

            player.sendMessage(Component.text("The sin has been lifted from your soul.", NamedTextColor.GREEN));
            this.playerDataManager.savePlayerData(player.getUniqueId());
        }
    }
}