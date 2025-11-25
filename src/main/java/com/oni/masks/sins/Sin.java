package com.oni.masks.sins;

import com.oni.masks.abilities.Ability;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.List;

@Getter
@RequiredArgsConstructor
public abstract class Sin {
    
    protected final SinType sinType;
    protected final Player player;
    
    public abstract List<Ability> getAbilities();
    public abstract List<PotionEffect> getPassiveEffects();
    public abstract void onEquip();
    public abstract void onUnequip();
    
    public String getName() {
        return this.sinType.getDisplayName();
    }
    
    public void activate() {
        final List<Ability> abilities = this.getAbilities();
        if (!abilities.isEmpty()) {
            abilities.get(0).use();
        }
    }
    
    public void applyPassiveEffects() {
        final List<PotionEffect> effects = this.getPassiveEffects();
        if (effects != null && !effects.isEmpty()) {
            for (final PotionEffect effect : effects) {
                this.player.addPotionEffect(effect);
            }
        }
    }
    
    public void removePassiveEffects() {
        final List<PotionEffect> effects = this.getPassiveEffects();
        if (effects != null && !effects.isEmpty()) {
            for (final PotionEffect effect : effects) {
                this.player.removePotionEffect(effect.getType());
            }
        }
    }
}