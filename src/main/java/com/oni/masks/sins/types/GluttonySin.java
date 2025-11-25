package com.oni.masks.sins.types;

import com.oni.masks.abilities.Ability;
import com.oni.masks.abilities.impl.FeastOfDecayAbility;
import com.oni.masks.sins.Sin;
import com.oni.masks.sins.SinType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class GluttonySin extends Sin {
    
    public GluttonySin(final Player player) {
        super(SinType.GLUTTONY, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(new FeastOfDecayAbility(this.player));
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        return List.of(
                new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 0, true, false)
        );
    }
    
    @Override
    public void onEquip() {
        this.player.sendMessage("§8§l[GLUTTONY] §7The endless hunger awakens...");
    }
    
    @Override
    public void onUnequip() {
        // Remove effects handled by parent class
    }
}