package com.oni.masks.sins.types;

import com.oni.masks.abilities.Ability;
import com.oni.masks.abilities.impl.CharmHealAbility;
import com.oni.masks.sins.Sin;
import com.oni.masks.sins.SinType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class LustSin extends Sin {
    
    public LustSin(final Player player) {
        super(SinType.LUST, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(new CharmHealAbility(this.player));
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        return List.of(
                new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false)
        );
    }
    
    @Override
    public void onEquip() {
        this.player.sendMessage("§d§l[LUST] §7Desire courses through your being...");
    }
    
    @Override
    public void onUnequip() {
        // Remove effects handled by parent class
    }
}