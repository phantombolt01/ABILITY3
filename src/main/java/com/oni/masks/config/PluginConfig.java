package com.oni.masks.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class PluginConfig {

    // Cooldowns
    private final int defaultCooldown;
    private final int flameMaskAbility1Cooldown;
    private final int earthMaskAbility1Cooldown;
    private final int earthMaskAbility2Cooldown;
    private final int waterMaskAbility1Cooldown;
    private final int lightMaskAbility1Cooldown;
    private final int lightMaskAbility2Cooldown;

    // Damage values
    private final double fireballDamage;
    private final double earthSpikeDamage;
    private final double waterWaveDamage;
    private final double lightBallDamage;

    // Particle settings
    private final double particleIntensity;
    private final double particleRange;

    // Event mask settings
    private final boolean eventMaskEnabled;
    private final int stage1RequiredKills;
    private final int stage2RequiredKills;
    private final int stage3RequiredKills;
    private final int stage4RequiredKills;

    // Recipe settings
    private final boolean eventMaskRecipeEnabled;
    private final boolean rerollItemRecipeEnabled;

    // Upgrade system settings
    private final int tier1RequiredKills;
    private final int tier2RequiredKills;
    private final boolean resetUpgradesOnDeath;
    private final int rerollCooldownSeconds;
    private final int eventCooldownSeconds;

    // Event Mask 3.2.0 settings
    private final int maxTargetsPerAbility;
    private final int batchSize;
    private final boolean lockInventoryDuringEvent;
    private final boolean removeEchoOnEventEnd;

    // Stage progression thresholds
    private final int stage1Threshold;
    private final int stage2Threshold;
    private final int stage3Threshold;
    private final int stage4Threshold;

    // Stage 1 - Shadow Hounds
    private final int houndSpawnCount;
    private final int houndHealth;
    private final double houndDamage;
    private final int houndLifetime;
    private final int houndParticleDensity;

    // Stage 2 - Black Parrots
    private final int parrotSpawnCount;
    private final int parrotHealth;
    private final double parrotDamage;
    private final int parrotPoisonDuration;
    private final int parrotParticleDensity;

    // Stage 3 - Killer Rabbits
    private final int rabbitSpawnCount;
    private final int rabbitHealth;
    private final double rabbitDamage;
    private final int rabbitWaveCooldown;
    private final int rabbitParticleDensity;

    // Stage 4 - Giant Panda
    private final int pandaHealth;
    private final double pandaDamage;
    private final double pandaKnockbackPower;
    private final int pandaResistanceLevel;
    private final int pandaParticleDensity;

    // Stage 5 - Shadow Wither
    private final int witherHealth;
    private final double witherSkullDamage;
    private final int witherSkullRadius;
    private final int witherSkullCooldown;
    private final int witherParticleDensity;

    // Void and Lightning mask settings
    private final int voidGraspCooldown;
    private final int voidEruptionCooldown;
    private final double voidDamage;
    private final int stormCallCooldown;
    private final int lightningBurstCooldown;
    private final double lightningDamage;

    public PluginConfig(final FileConfiguration config) {
        // Load cooldowns
        this.defaultCooldown = config.getInt("cooldowns.default", 2);
        this.flameMaskAbility1Cooldown = config.getInt("cooldowns.flame-mask.ability1", 2);
        this.earthMaskAbility1Cooldown = config.getInt("cooldowns.earth-mask.ability1", 3);
        this.earthMaskAbility2Cooldown = config.getInt("cooldowns.earth-mask.ability2", 5);
        this.waterMaskAbility1Cooldown = config.getInt("cooldowns.water-mask.ability1", 2);
        this.lightMaskAbility1Cooldown = config.getInt("cooldowns.light-mask.ability1", 4);
        this.lightMaskAbility2Cooldown = config.getInt("cooldowns.light-mask.ability2", 2);

        // Load damage values
        this.fireballDamage = config.getDouble("damage.fireball", 4.0);
        this.earthSpikeDamage = config.getDouble("damage.earth-spike", 3.5);
        this.waterWaveDamage = config.getDouble("damage.water-wave", 3.0);
        this.lightBallDamage = config.getDouble("damage.light-ball", 2.0);

        // Load particle settings
        this.particleIntensity = config.getDouble("particles.intensity", 1.0);
        this.particleRange = config.getDouble("particles.range", 32.0);

        // Load event mask settings
        this.eventMaskEnabled = config.getBoolean("event-mask.enabled", false);
        this.stage1RequiredKills = config.getInt("event-mask.progression.stage1-kills", 5);
        this.stage2RequiredKills = config.getInt("event-mask.progression.stage2-kills", 10);
        this.stage3RequiredKills = config.getInt("event-mask.progression.stage3-kills", 15);
        this.stage4RequiredKills = config.getInt("event-mask.progression.stage4-kills", 20);

        // Load recipe settings
        this.eventMaskRecipeEnabled = config.getBoolean("recipes.event-mask.enabled", false); // Disabled by default
        this.rerollItemRecipeEnabled = config.getBoolean("recipes.reroll-item.enabled", true);

        // Load upgrade system settings
        this.tier1RequiredKills = config.getInt("upgrades.tier1-kills", 5);
        this.tier2RequiredKills = config.getInt("upgrades.tier2-kills", 15);
        this.resetUpgradesOnDeath = config.getBoolean("upgrades.reset-on-death", false);
        this.rerollCooldownSeconds = config.getInt("upgrades.reroll-cooldown", 20);
        this.eventCooldownSeconds = config.getInt("event.cooldown-seconds", 60);

        // Load Event Mask settings
        this.maxTargetsPerAbility = config.getInt("event.maxTargetsPerAbility", 100);
        this.batchSize = config.getInt("event.batchSize", 50);
        this.lockInventoryDuringEvent = config.getBoolean("event.lockInventoryDuringEvent", true);
        this.removeEchoOnEventEnd = config.getBoolean("event.removeEchoOnEventEnd", true);

        // Load stage thresholds
        this.stage1Threshold = config.getInt("event.stageThresholds.stage1", 10);
        this.stage2Threshold = config.getInt("event.stageThresholds.stage2", 25);
        this.stage3Threshold = config.getInt("event.stageThresholds.stage3", 60);
        this.stage4Threshold = config.getInt("event.stageThresholds.stage4", 120);

        // Load Stage 1 - Shadow Hounds
        this.houndSpawnCount = config.getInt("event.stage1.hound.spawnCount", 2);
        this.houndHealth = config.getInt("event.stage1.hound.health", 40);
        this.houndDamage = config.getDouble("event.stage1.hound.damage", 6.0);
        this.houndLifetime = config.getInt("event.stage1.hound.lifetime", 60);
        this.houndParticleDensity = config.getInt("event.stage1.hound.particleDensity", 28);

        // Load Stage 2 - Black Parrots
        this.parrotSpawnCount = config.getInt("event.stage2.parrot.spawnCount", 12);
        this.parrotHealth = config.getInt("event.stage2.parrot.health", 4);
        this.parrotDamage = config.getDouble("event.stage2.parrot.damage", 1.0);
        this.parrotPoisonDuration = config.getInt("event.stage2.parrot.poisonDuration", 3);
        this.parrotParticleDensity = config.getInt("event.stage2.parrot.particleDensity", 18);

        // Load Stage 3 - Killer Rabbits
        this.rabbitSpawnCount = config.getInt("event.stage3.rabbit.spawnCount", 8);
        this.rabbitHealth = config.getInt("event.stage3.rabbit.health", 6);
        this.rabbitDamage = config.getDouble("event.stage3.rabbit.damage", 3.0);
        this.rabbitWaveCooldown = config.getInt("event.stage3.rabbit.waveCooldown", 8);
        this.rabbitParticleDensity = config.getInt("event.stage3.rabbit.particleDensity", 22);

        // Load Stage 4 - Giant Panda
        this.pandaHealth = config.getInt("event.stage4.panda.health", 120);
        this.pandaDamage = config.getDouble("event.stage4.panda.damage", 14.0);
        this.pandaKnockbackPower = config.getDouble("event.stage4.panda.knockbackPower", 1.2);
        this.pandaResistanceLevel = config.getInt("event.stage4.panda.resistanceLevel", 2);
        this.pandaParticleDensity = config.getInt("event.stage4.panda.particleDensity", 36);

        // Load Stage 5 - Shadow Wither
        this.witherHealth = config.getInt("event.stage5.wither.health", 200); // 200 HP = 100 hearts
        this.witherSkullDamage = config.getDouble("event.stage5.wither.skullDamage", 6.0); // ~3 hearts = 6 HP
        this.witherSkullRadius = config.getInt("event.stage5.wither.skullRadius", 14);
        this.witherSkullCooldown = config.getInt("event.stage5.wither.skullCooldown", 10);
        this.witherParticleDensity = config.getInt("event.stage5.wither.particleDensity", 48);
        

        // Load Void and Lightning settings
        this.voidGraspCooldown = config.getInt("cooldowns.void-mask.grasp", 10);
        this.voidEruptionCooldown = config.getInt("cooldowns.void-mask.eruption", 20);
        this.voidDamage = config.getDouble("damage.void", 8.0);
        this.stormCallCooldown = config.getInt("cooldowns.lightning-mask.storm-call", 10);
        this.lightningBurstCooldown = config.getInt("cooldowns.lightning-mask.burst", 20);
        this.lightningDamage = config.getDouble("damage.lightning", 8.0);
    }
}
