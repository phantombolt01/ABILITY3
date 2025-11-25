package com.oni.masks.sins.types;

import com.oni.masks.abilities.Ability;
import com.oni.masks.abilities.impl.DragonBreathSurgeAbility;
import com.oni.masks.sins.Sin;
import com.oni.masks.sins.SinType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class PrideSin extends Sin {
    
    public PrideSin(final Player player) {
        super(SinType.PRIDE, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(new DragonBreathSurgeAbility(this.player));
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        return List.of(
                new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false)
        );
    }
    
    @Override
    public void onEquip() {
        this.player.sendMessage("§5§l[PRIDE] §7The crown of arrogance weighs heavy upon you...");
    }
    
    @Override
    public void onUnequip() {
        // Remove effects handled by parent class
    }
}