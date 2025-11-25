package com.oni.masks.masks.types;

import com.oni.masks.abilities.Ability;
import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.impl.HealingAuraAbility;
import com.oni.masks.abilities.impl.LightOrbVolleyAbility;
import com.oni.masks.masks.Mask;
import com.oni.masks.masks.MaskType;
import com.oni.masks.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class LightMask extends Mask {
    
private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public LightMask(final Player player) {
        super(MaskType.LIGHT, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new HealingAuraAbility(this.player),
                new LightOrbVolleyAbility(this.player)
        );
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        final PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(this.player.getUniqueId());
        final int tier = playerData.getMaskTier();
        
        final List<PotionEffect> effects = new ArrayList<>();
        effects.add(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, true, false));
        
        // Tier-based Speed upgrades
        if (tier >= 1) {
            effects.add(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 1, true, false));
        }
        if (tier >= 2) {
            effects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
        }
        
        return effects;
    }
    
    @Override
    public void onEquip() {
        // Play bell/beacon sound
        // Spawn light particles
    }
    
    @Override
    public void onUnequip() {
        // Remove effects handled by parent class
    }
}