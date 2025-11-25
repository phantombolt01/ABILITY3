package com.oni.masks.masks.types;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.abilities.impl.ForbiddenShadowsAbility;
import com.oni.masks.masks.Mask;
import com.oni.masks.masks.MaskType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class ForbiddenShadowsMask extends Mask {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public ForbiddenShadowsMask(final Player player) {
        super(MaskType.FORBIDDEN_SHADOWS, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new ForbiddenShadowsAbility(this.player)
        );
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        return List.of(
                new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false),
                new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, true, false)
        );
    }
    
    @Override
    public void onEquip() {
        // Play dark ambient sound
        this.plugin.getSoundManager().playEventMaskActivation();
        
        // Spawn dark particles around player
        this.plugin.getParticleManager().playEventMaskActivation(this.player);
        
        // Send dramatic message
        this.player.sendMessage(Component.text("§8§lThe shadows bend to your will...", NamedTextColor.DARK_GRAY));
    }
    
    @Override
    public void onUnequip() {
        // Clear any summoned mobs when mask is removed
        this.plugin.getEventManager().getMobProgressionManager().clearPlayerMobs(this.player.getUniqueId());
    }
}