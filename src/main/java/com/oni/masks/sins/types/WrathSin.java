package com.oni.masks.sins.types;

import com.oni.masks.abilities.Ability;
import com.oni.masks.abilities.impl.FirestormAbility;
import com.oni.masks.sins.Sin;
import com.oni.masks.sins.SinType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class WrathSin extends Sin {
    
    public WrathSin(final Player player) {
        super(SinType.WRATH, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(new FirestormAbility(this.player));
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        return List.of(
                new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, true, false)
        );
    }
    
    @Override
    public void onEquip() {
        this.player.sendMessage("§c§l[WRATH] §7Fury burns eternal within your soul...");
    }
    
    @Override
    public void onUnequip() {
        // Remove effects handled by parent class
    }
}