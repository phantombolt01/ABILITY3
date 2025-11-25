package com.oni.masks.sins.types;

import com.oni.masks.abilities.Ability;
import com.oni.masks.abilities.impl.JealousBurstAbility;
import com.oni.masks.sins.Sin;
import com.oni.masks.sins.SinType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class EnvySin extends Sin {
    
    public EnvySin(final Player player) {
        super(SinType.ENVY, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(new JealousBurstAbility(this.player));
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        return List.of(
                new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 2, true, false)
        );
    }
    
    @Override
    public void onEquip() {
        this.player.sendMessage("§a§l[ENVY] §7The green hunger consumes your heart...");
    }
    
    @Override
    public void onUnequip() {
        // Remove effects handled by parent class
    }
}