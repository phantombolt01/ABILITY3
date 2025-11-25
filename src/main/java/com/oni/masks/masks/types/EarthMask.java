package com.oni.masks.masks.types;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.abilities.impl.EarthSpikeAbility;
import com.oni.masks.abilities.impl.SpikeShieldAbility;
import com.oni.masks.masks.Mask;
import com.oni.masks.masks.MaskType;
import com.oni.masks.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class EarthMask extends Mask {
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public EarthMask(final Player player) {
        super(MaskType.EARTH, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new EarthSpikeAbility(this.player),
                new SpikeShieldAbility(this.player)
        );
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        final PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(this.player.getUniqueId());
        final int tier = playerData.getMaskTier();
        
        final List<PotionEffect> effects = new ArrayList<>();
        effects.add(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, true, false));
        
        // Tier-based improvements
        if (tier >= 1) {
            effects.add(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 0, true, false)); // Reduced slowness
        } else {
            effects.add(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 1, true, false)); // More slowness at tier 0
        }
        
        if (tier >= 2) {
            effects.add(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, 2, true, false));
            effects.add(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 0, true, false));
        }
        
        return effects;
    }
    
    @Override
    public void onEquip() {
        // Play earth rumble sound
        // Spawn earth particles
    }
    
    @Override
    public void onUnequip() {
        // Remove effects handled by parent class
    }
}