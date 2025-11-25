package com.oni.masks.masks.types;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.abilities.impl.PrimordialFlameAbility;
import com.oni.masks.masks.Mask;
import com.oni.masks.masks.MaskType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class PrimordialFlameMask extends Mask {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public PrimordialFlameMask(final Player player) {
        super(MaskType.PRIMORDIAL_FLAME, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new PrimordialFlameAbility(this.player)
        );
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        return List.of(
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false),
                new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, true, false) // Strength I
        );
    }
    
    @Override
    public void onEquip() {
        // Play massive fire ignition sound
        this.plugin.getSoundManager().playPrimordialFlameActivation();
        
        // Spawn intense flame particles around player
        this.plugin.getParticleManager().playPrimordialFlameActivation(this.player);
        
        // Send epic message
        this.player.sendMessage(Component.text("§6§lThe primordial flames surge through your being!", NamedTextColor.GOLD));
    }
    
    @Override
    public void onUnequip() {
        // Remove flame effects
    }
}