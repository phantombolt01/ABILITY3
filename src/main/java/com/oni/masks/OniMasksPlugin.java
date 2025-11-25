package com.oni.masks;

import com.oni.masks.commands.ConfigCommand;
import com.oni.masks.commands.DeveloperCommand;
import com.oni.masks.commands.EventCommand;
import com.oni.masks.commands.PluginInfoCommand;
import com.oni.masks.commands.RerollCommand;
import com.oni.masks.commands.SosCommand;
import com.oni.masks.commands.TrustCommand;
import com.oni.masks.commands.AbilityCommand;
import com.oni.masks.config.ConfigManager;
import com.oni.masks.effects.ParticleManager;
import com.oni.masks.effects.SoundManager;
import com.oni.masks.events.EventManager;
import com.oni.masks.items.ItemManager;
import com.oni.masks.listeners.CraftingListener;
import com.oni.masks.listeners.PlayerInteractListener;
import com.oni.masks.listeners.PlayerJoinListener;
import com.oni.masks.listeners.PlayerDeathListener;
import com.oni.masks.masks.MaskManager;
import com.oni.masks.player.PlayerDataManager;
import com.oni.masks.player.TrustManager;
import com.oni.masks.sins.SinManager;
import com.oni.masks.ui.ActionBarManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class OniMasksPlugin extends JavaPlugin {

    private static OniMasksPlugin instance;
    
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private MaskManager maskManager;
    private TrustManager trustManager;
    private ActionBarManager actionBarManager;
    private ParticleManager particleManager;
    private SoundManager soundManager;
    private ItemManager itemManager;
    private EventManager eventManager;
    private DeveloperCommand developerCommand;
    private SinManager sinManager;

    @Override
    public void onEnable() {
        instance = this;
        
        this.getLogger().info("Starting Oni Masks Plugin v12.0.0... SUP IAM PHANTOM");
        
        // Initialize managers in correct order
        this.configManager = new ConfigManager();
        this.playerDataManager = new PlayerDataManager();
        this.trustManager = new TrustManager();
        this.particleManager = new ParticleManager();
        this.soundManager = new SoundManager();
        this.maskManager = new MaskManager();
        this.itemManager = new ItemManager();
        this.eventManager = new EventManager();
        this.actionBarManager = new ActionBarManager();
        this.developerCommand = new DeveloperCommand(this); 
        this.sinManager = new SinManager();
        
        // Register events
        this.registerEvents();
        
        // Register commands
        this.registerCommands();
        
        // Register developer command as listener too
        this.getServer().getPluginManager().registerEvents(this.developerCommand, this);

        this.getLogger().info("Oni Masks Plugin v12.0.0 enabled successfully! YESSSS STARTING THIS SERVER FINALLY YESSS");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabling Oni Masks Plugin...NOOOOOOOOOOOOO");
        
        // Save all player data
        if (this.playerDataManager != null) {
            this.playerDataManager.saveAllPlayerData();
        }
        
        // Stop action bar updates
        if (this.actionBarManager != null) {
            this.actionBarManager.shutdown();
        }
        
        // Clear all event mobs
        if (this.eventManager != null) {
            this.eventManager.getMobProgressionManager().clearAllMobs();
        }
        
        // Shutdown developer command
        if (this.developerCommand != null) {
            this.developerCommand.shutdown();
        }
        
        this.getLogger().info("Oni Masks Plugin disabled!.. BRO WHY WHY WOULD U DISABLE IT");
    }
    
    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        this.getServer().getPluginManager().registerEvents(new CraftingListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
    }
    
    private void registerCommands() {
        this.getCommand("trust").setExecutor(new TrustCommand());
        this.getCommand("untrust").setExecutor(new TrustCommand());
        this.getCommand("config").setExecutor(new ConfigCommand());
        this.getCommand("event").setExecutor(new EventCommand());
        this.getCommand("reroll").setExecutor(new RerollCommand());
        this.getCommand("plugininfo").setExecutor(new PluginInfoCommand());
        this.getCommand("developer").setExecutor(developerCommand);
        this.getCommand("sos").setExecutor(new SosCommand());
        
        // Register ability commands
        final AbilityCommand abilityCommand = new AbilityCommand();
        this.getCommand("ability1").setExecutor(abilityCommand);
        this.getCommand("ability2").setExecutor(abilityCommand);
    }
    
    // Explicit getters (no Lombok dependency issues)
    public static OniMasksPlugin getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return this.configManager;
    }
    
    public PlayerDataManager getPlayerDataManager() {
        return this.playerDataManager;
    }
    
    public MaskManager getMaskManager() {
        return this.maskManager;
    }
    
    public TrustManager getTrustManager() {
        return this.trustManager;
    }
    
    public ActionBarManager getActionBarManager() {
        return this.actionBarManager;
    }
    
    public ParticleManager getParticleManager() {
        return this.particleManager;
    }
    
    public SoundManager getSoundManager() {
        return this.soundManager;
    }
    
    public ItemManager getItemManager() {
        return this.itemManager;
    }
    
    public EventManager getEventManager() {
        return this.eventManager;
    }
    
    public SinManager getSinManager() {
        return this.sinManager;
    }
}
