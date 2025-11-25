package com.oni.masks.masks.types;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.abilities.impl.VoidGraspAbility;
import com.oni.masks.abilities.impl.VoidEruptionAbility;
import com.oni.masks.masks.Mask;
import com.oni.masks.masks.MaskType;
import com.oni.masks.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class VoidMask extends Mask {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public VoidMask(final Player player) {
        super(MaskType.VOID, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new VoidGraspAbility(this.player),
                new VoidEruptionAbility(this.player)
        );
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        final PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(this.player.getUniqueId());
        final int tier = playerData.getTierLevel();
        
        final List<PotionEffect> effects = new ArrayList<>();
        effects.add(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false));
        
        // Tier-based improvements
        if (tier >= 1) {
            effects.add(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, 1, true, false));
        }
        
        if (tier >= 2) {
            effects.add(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, true, false));
        }
        
        return effects;
    }
    
    @Override
    public void onEquip() {
    
        
        // Spawn dark particles around player
        this.plugin.getParticleManager().playVoidActivation(this.player);
        
        // Send dramatic message
        this.player.sendMessage(net.kyori.adventure.text.Component.text("§8§lThe abyss does not whisper; it drags...", 
                net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY));
    }
    
    @Override
    public void onUnequip() {
        // Clear any void effects
    }
}