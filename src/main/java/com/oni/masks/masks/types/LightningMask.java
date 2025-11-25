package com.oni.masks.masks.types;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.abilities.Ability;
import com.oni.masks.abilities.impl.StormCallAbility;
import com.oni.masks.abilities.impl.LightningBurstAbility;
import com.oni.masks.masks.Mask;
import com.oni.masks.masks.MaskType;
import com.oni.masks.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class LightningMask extends Mask {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    
    public LightningMask(final Player player) {
        super(MaskType.LIGHTNING, player);
    }
    
    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new StormCallAbility(this.player),
                new LightningBurstAbility(this.player)
        );
    }
    
    @Override
    public List<PotionEffect> getPassiveEffects() {
        final PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(this.player.getUniqueId());
        final int tier = playerData.getTierLevel();
        
        final List<PotionEffect> effects = new ArrayList<>();
        effects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
        
        // Tier-based improvements
        if (tier >= 1) {
            effects.add(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 2, true, false));
        }
        
        if (tier >= 2) {
            effects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
        }
        
        return effects;
    }
    
    @Override
    public void onEquip() {
        // Play thunder activation sound
        
        // Spawn lightning particles around player
        this.plugin.getParticleManager().playLightningActivation(this.player);
        
        // Send dramatic message
        this.player.sendMessage(net.kyori.adventure.text.Component.text("§e§lThe storm does not ask. It consumes.", 
                net.kyori.adventure.text.format.NamedTextColor.YELLOW));
    }
    
    @Override
    public void onUnequip() {
        // Clear any lightning effects
    }
}