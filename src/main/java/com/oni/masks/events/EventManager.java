package com.oni.masks.events;

import com.oni.masks.OniMasksPlugin;
import lombok.Getter;

public class EventManager {

    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();

    @Getter
    private final MobProgressionManager mobProgressionManager = new MobProgressionManager();

    public EventManager() {
        this.plugin.getServer().getPluginManager().registerEvents(this.mobProgressionManager, this.plugin);
    }
}