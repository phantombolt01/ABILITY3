package com.oni.masks.player;

import com.oni.masks.masks.Mask;
import com.oni.masks.masks.MaskType;
import com.oni.masks.sins.Sin;
import com.oni.masks.sins.SinType;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
public class PlayerData {
    
    private UUID playerId;
    private MaskType maskType;
    private transient Mask currentMask; // Don't serialize the actual mask object
    private SinType sinType;
    private transient Sin currentSin; // Don't serialize the actual sin object
    private boolean hasJoinedBefore;
    private Set<UUID> trustedPlayers;
    private Map<String, Long> abilityCooldowns;
    private int currentEventStage;
    private int eventStageXP;
    private int playerKills;
    private int maskTier;
    private MaskType previousMaskType; // Store mask before event
    private long lastRerollTime; // Anti-spam for reroll items
    private Set<UUID> uniqueKills; // Track unique player kills for tier system
    private int tierLevel; // Current tier (0, 1, 2)
    
    public PlayerData(final UUID playerId) {
        this.playerId = playerId;
        this.hasJoinedBefore = false;
        this.trustedPlayers = new HashSet<>();
        this.abilityCooldowns = new HashMap<>();
        this.currentEventStage = 1;
        this.eventStageXP = 0;
        this.playerKills = 0;
        this.maskTier = 0;
        this.previousMaskType = null;
        this.lastRerollTime = 0;
        this.uniqueKills = new HashSet<>();
        this.tierLevel = 0;
    }
    
    public boolean isTrusted(final UUID otherPlayerId) {
        return this.trustedPlayers.contains(otherPlayerId);
    }
    
    public void trustPlayer(final UUID otherPlayerId) {
        this.trustedPlayers.add(otherPlayerId);
    }
    
    public void untrustPlayer(final UUID otherPlayerId) {
        this.trustedPlayers.remove(otherPlayerId);
    }
    
    public boolean isAbilityOnCooldown(final String abilityName) {
        final Long cooldownEnd = this.abilityCooldowns.get(abilityName);
        return cooldownEnd != null && System.currentTimeMillis() < cooldownEnd;
    }
    
    public void setCooldown(final String abilityName, final long durationMs) {
        this.abilityCooldowns.put(abilityName, System.currentTimeMillis() + durationMs);
    }
    
    public long getRemainingCooldown(final String abilityName) {
        final Long cooldownEnd = this.abilityCooldowns.get(abilityName);
        if (cooldownEnd == null) {
            return 0;
        }
        return Math.max(0, cooldownEnd - System.currentTimeMillis());
    }
    
    public void incrementEventStageXP() {
        this.eventStageXP++;
    }
    
    public void resetEventProgress() {
        this.currentEventStage = 1;
        this.eventStageXP = 0;
    }
    
    public boolean canUseReroll() {
        return System.currentTimeMillis() - this.lastRerollTime > 20000; // 20 seconds
    }
    
    public boolean addUniqueKill(final UUID victimId) {
        if (this.uniqueKills.add(victimId)) {
            // Check for tier upgrades
            final int kills = this.uniqueKills.size();
            int newTier = 0;
            
            if (kills >= 4) {
                newTier = 2;
            } else if (kills >= 2) {
                newTier = 1;
            }
            
            if (newTier > this.tierLevel) {
                this.tierLevel = newTier;
                return true; // Tier upgraded
            }
        }
        return false; // No tier change
    }
    
    public int getUniqueKillCount() {
        return this.uniqueKills.size();
    }
}