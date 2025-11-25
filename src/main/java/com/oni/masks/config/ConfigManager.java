package com.oni.masks.config;

import com.oni.masks.OniMasksPlugin;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

@Getter
public class ConfigManager {
    
    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private FileConfiguration config;
    private File configFile;
    private PluginConfig pluginConfig;
    
    public ConfigManager() {
        this.loadConfig();
        this.pluginConfig = new PluginConfig(this.config);
    }
    
    private void loadConfig() {
        this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
        
        if (!this.configFile.exists()) {
            this.plugin.saveDefaultConfig();
        }
        
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        this.setDefaults();
    }
    
    private void setDefaults() {
        // Default cooldowns (in seconds)
        this.config.addDefault("cooldowns.default", 2);
        this.config.addDefault("cooldowns.flame-mask.ability1", 2);
        this.config.addDefault("cooldowns.earth-mask.ability1", 3);
        this.config.addDefault("cooldowns.earth-mask.ability2", 5);
        this.config.addDefault("cooldowns.water-mask.ability1", 2);
        this.config.addDefault("cooldowns.light-mask.ability1", 4);
        this.config.addDefault("cooldowns.light-mask.ability2", 2);
        
        // Default damage values
        this.config.addDefault("damage.fireball", 4.0);
        this.config.addDefault("damage.earth-spike", 3.5);
        this.config.addDefault("damage.water-wave", 3.0);
        this.config.addDefault("damage.light-ball", 2.0);
        
        // Particle settings
        this.config.addDefault("particles.intensity", 1.0);
        this.config.addDefault("particles.range", 32.0);
        
        // Event mask settings
        this.config.addDefault("event-mask.enabled", false);
        this.config.addDefault("event-mask.progression.stage1-kills", 5);
        this.config.addDefault("event-mask.progression.stage2-kills", 10);
        this.config.addDefault("event-mask.progression.stage3-kills", 15);
        this.config.addDefault("event-mask.progression.stage4-kills", 20);
        
        // Recipe settings
        this.config.addDefault("recipes.event-mask.enabled", false); // Event masks are admin-only
        this.config.addDefault("recipes.reroll-item.enabled", true);
        
        // Upgrade system settings
        this.config.addDefault("upgrades.tier1-kills", 5);
        this.config.addDefault("upgrades.tier2-kills", 15);
        this.config.addDefault("upgrades.reset-on-death", false);
        this.config.addDefault("upgrades.reroll-cooldown", 20);
        this.config.addDefault("event.cooldown-seconds", 60);
        
        // Event Mask 3.2.0 defaults
        this.config.addDefault("event.maxTargetsPerAbility", 100);
        this.config.addDefault("event.batchSize", 50);
        this.config.addDefault("event.lockInventoryDuringEvent", true);
        this.config.addDefault("event.removeEchoOnEventEnd", true);
        
        // Stage thresholds
        this.config.addDefault("event.stageThresholds.stage1", 10);
        this.config.addDefault("event.stageThresholds.stage2", 25);
        this.config.addDefault("event.stageThresholds.stage3", 60);
        this.config.addDefault("event.stageThresholds.stage4", 120);
        
        // Stage 1 - Shadow Hounds
        this.config.addDefault("event.stage1.hound.spawnCount", 2);
        this.config.addDefault("event.stage1.hound.health", 40);
        this.config.addDefault("event.stage1.hound.damage", 6.0);
        this.config.addDefault("event.stage1.hound.lifetime", 60);
        this.config.addDefault("event.stage1.hound.particleDensity", 28);
        
        // Stage 2 - Black Parrots
        this.config.addDefault("event.stage2.parrot.spawnCount", 12);
        this.config.addDefault("event.stage2.parrot.health", 4);
        this.config.addDefault("event.stage2.parrot.damage", 1.0);
        this.config.addDefault("event.stage2.parrot.poisonDuration", 3);
        this.config.addDefault("event.stage2.parrot.particleDensity", 18);
        
        // Stage 3 - Killer Rabbits
        this.config.addDefault("event.stage3.rabbit.spawnCount", 8);
        this.config.addDefault("event.stage3.rabbit.health", 6);
        this.config.addDefault("event.stage3.rabbit.damage", 3.0);
        this.config.addDefault("event.stage3.rabbit.waveCooldown", 8);
        this.config.addDefault("event.stage3.rabbit.particleDensity", 22);
        
        // Stage 4 - Giant Panda
        this.config.addDefault("event.stage4.panda.health", 120);
        this.config.addDefault("event.stage4.panda.damage", 14.0);
        this.config.addDefault("event.stage4.panda.knockbackPower", 1.2);
        this.config.addDefault("event.stage4.panda.resistanceLevel", 2);
        this.config.addDefault("event.stage4.panda.particleDensity", 36);
        
        // Stage 5 - Shadow Warden
        this.config.addDefault("event.stage5.warden.health", 200);
        this.config.addDefault("event.stage5.warden.beamDamage", 35.0);
        this.config.addDefault("event.stage5.warden.beamRadius", 14);
        this.config.addDefault("event.stage5.warden.beamCooldown", 10);
        this.config.addDefault("event.stage5.warden.particleDensity", 48);
        
        // Void and Lightning mask defaults
        this.config.addDefault("cooldowns.void-mask.grasp", 25);
        this.config.addDefault("cooldowns.void-mask.eruption", 35);
        this.config.addDefault("damage.void", 8.0);
        this.config.addDefault("cooldowns.lightning-mask.storm-call", 35);
        this.config.addDefault("cooldowns.lightning-mask.burst", 25);
        this.config.addDefault("damage.lightning", 8.0);
        
        this.config.options().copyDefaults(true);
        this.saveConfig();
    }
    
    public void saveConfig() {
        try {
            this.config.save(this.configFile);
            this.pluginConfig = new PluginConfig(this.config);
        } catch (IOException exception) {
            this.plugin.getLogger().severe("Could not save config: " + exception.getMessage());
        }
    }
    
    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        this.pluginConfig = new PluginConfig(this.config);
    }
    
    public void setValue(final String path, final Object value) {
        this.config.set(path, value);
        this.saveConfig();
    }
}