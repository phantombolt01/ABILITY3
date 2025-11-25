package com.oni.masks.sins.types;

import com.oni.masks.abilities.Ability;
import com.oni.masks.abilities.impl.WeightOfTheWorldAbility;
import com.oni.masks.sins.Sin;
import com.oni.masks.sins.SinType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class SlothSin extends Sin {
    
    public SlothSin(final Player player) {
        super(SinType.SLOTH, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(new WeightOfTheWorldAbility(this.player));
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        return List.of(
                new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, true, false)
        );
    }
    
    @Override
    public void onEquip() {
        this.player.sendMessage("§7§l[SLOTH] §7The weight of eternity settles upon you...");
    }
    
    @Override
    public void onUnequip() {
        // Remove effects handled by parent class
    }
}