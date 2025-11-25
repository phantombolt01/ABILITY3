package com.oni.masks.sins.types;

import com.oni.masks.abilities.Ability;
import com.oni.masks.abilities.impl.GoldenPillageAbility;
import com.oni.masks.sins.Sin;
import com.oni.masks.sins.SinType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class GreedSin extends Sin {
    
    public GreedSin(final Player player) {
        super(SinType.GREED, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(new GoldenPillageAbility(this.player));
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        return List.of(
                new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, 0, true, false),
                new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 0, true, false),
                new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, true, false)
        );
    }
    
    @Override
    public void onEquip() {
        this.player.sendMessage("§6§l[GREED] §7Gold flows through your veins like blood...");
    }
    
    @Override
    public void onUnequip() {
        // Remove effects handled by parent class
    }
}