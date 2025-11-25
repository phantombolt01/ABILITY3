package com.oni.masks.masks.types;

import com.oni.masks.abilities.Ability;
import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.impl.WaterWaveAbility;
import com.oni.masks.abilities.impl.WaterDashAbility;
import com.oni.masks.masks.Mask;
import com.oni.masks.masks.MaskType;
import com.oni.masks.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class WaterMask extends Mask {
    
private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public WaterMask(final Player player) {
        super(MaskType.WATER, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new WaterWaveAbility(this.player),
                new WaterDashAbility(this.player)
        );
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        final PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(this.player.getUniqueId());
        final int tier = playerData.getMaskTier();
        
        final List<PotionEffect> effects = new ArrayList<>();
        effects.add(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 3, true, false));
        
        // Tier-based Speed upgrades
        if (tier >= 1) {
            effects.add(new PotionEffect(PotionEffectType.CONDUIT_POWER, Integer.MAX_VALUE, 3, true, false));
        }
        if (tier >= 2) {
            effects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
        }
        
        return effects;
    }
    
    @Override
    public void onEquip() {
        // Play water splash sound
        // Spawn water particles
    }
    
    @Override
    public void onUnequip() {
        // Remove effects handled by parent class
    }
}