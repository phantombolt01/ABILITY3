package com.oni.masks.masks.types;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.abilities.impl.FireballBarrageAbility;
import com.oni.masks.abilities.impl.FlameBlastAbility;
import com.oni.masks.masks.Mask;
import com.oni.masks.masks.MaskType;
import com.oni.masks.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.List;
import java.util.ArrayList; 

public class FlameMask extends Mask {
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public FlameMask(final Player player) {
        super(MaskType.FLAME, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new FireballBarrageAbility(this.player),
                new FlameBlastAbility(this.player)
        );
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        final PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(this.player.getUniqueId());
        final int tier = playerData.getMaskTier();
        
        final List<PotionEffect> effects = new ArrayList<>(); 
        effects.add(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
        
        // Tier-based Strength upgrades
        if (tier >= 1) {
            effects.add(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 1, true, false));
        } else {
            effects.add(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 1, true, false));
        }
        if (tier >= 2) {
            effects.add(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, true, false));
        }
        
        return effects; 
    }
    
    @Override
    public void onEquip() {
        // Play flame ignition sound
        // Spawn flame particles above player
    }
    
    @Override
    public void onUnequip() {
        // Remove effects handled by parent class
    }
}
