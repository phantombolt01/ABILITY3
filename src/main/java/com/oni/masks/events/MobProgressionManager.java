package com.oni.masks.events;

import com.oni.masks.OniMasksPlugin;
import com.oni.masks.config.PluginConfig;
import com.oni.masks.player.TrustManager;
import com.oni.masks.player.PlayerData;
import com.oni.masks.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * MobProgressionManager
 *
 * - Summons stage mobs (including Shadow Wither) for event mask progression.
 * - Keeps many behaviours intact from previous implementation but adjusts Wither behaviour
 *   to meet the "Shadow Wither" spec:
 *     • Vanilla Wither AI (we do not replace movement/attack AI)
 *     • No block griefing / no explosion damage
 *     • Skulls deal 4 HP (2 hearts) of true damage (bypass armor)
 *     • Targets only untrusted players (uses TrustManager)
 *     • Keeps within ~10 blocks of owner and teleports back if too far
 *     • Despawns on owner logout, respawns on owner login (if stage/active)
 *
 * Boss bar handling:
 * For Paper 1.21 we can reliably hide the Wither bossbar by using the Bukkit Boss API:
 *   - we call ((Boss) wither).getBossBar().removeAll() repeatedly while the Wither exists.
 * This avoids version-specific NMS and works well on Paper.
 */
public class MobProgressionManager implements Listener {

    private final OniMasksPlugin plugin = OniMasksPlugin.getInstance();
    private final PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
    private final TrustManager trustManager = this.plugin.getTrustManager();

    private final Map<UUID, List<LivingEntity>> playerSummonedMobs = new HashMap<>();
    private final Map<UUID, Integer> playerCurrentStage = new HashMap<>();
    private final Map<UUID, Long> eventCooldowns = new HashMap<>();
    private final Map<UUID, UUID> mobOwnerMap = new HashMap<>(); // mob UUID -> owner UUID
    private final Map<UUID, UUID> lastDamagerMap = new HashMap<>(); // entity UUID -> last damager UUID

    // Lock to prevent accidental re-activation / double-start while event active
    private final Map<UUID, Boolean> eventActive = new HashMap<>();

    private final NamespacedKey eventOwnerKey = new NamespacedKey(this.plugin, "event_owner");
    private final NamespacedKey eventSummonedKey = new NamespacedKey(this.plugin, "event_summoned");

    public void activateEventMask(final Player player) {
        final UUID playerId = player.getUniqueId();

        // Prevent activating while event already active for this player
        if (Boolean.TRUE.equals(this.eventActive.get(playerId))) {
            player.sendMessage(Component.text("§cYour event is already active.", NamedTextColor.RED));
            return;
        }

        // Check event cooldown
        if (this.isOnEventCooldown(playerId)) {
            final long remainingTime = (this.eventCooldowns.get(playerId) - System.currentTimeMillis()) / 1000;
            player.sendMessage(Component.text("§cEvent on cooldown — wait " + remainingTime + "s before triggering again.", NamedTextColor.RED));
            return;
        }

        final PlayerData playerData = this.playerDataManager.getPlayerData(playerId);

        // Mark event active
        this.eventActive.put(playerId, true);

        // Clear existing mobs first
        this.clearPlayerMobs(playerId);

        // Always start at Stage 1
        this.playerCurrentStage.put(playerId, 1);
        if (playerData != null) {
            playerData.setCurrentEventStage(1);
            playerData.setEventStageXP(0);
        }

        // Summon Stage 1 mobs
        this.summonMobsForStage(player, 1);

        // Play dramatic activation effects
        this.plugin.getParticleManager().playEventMaskActivation(player);
        this.plugin.getSoundManager().playEventMaskActivation();

        // Send activation message
        final Component message = Component.text()
                .append(Component.text("⚡ [Event] ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
                .append(Component.text("Dark forces awaken for ", NamedTextColor.GRAY))
                .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                .append(Component.text("!", NamedTextColor.GRAY))
                .build();

        this.plugin.getServer().broadcast(message);

        // Set cooldown for next activation (configurable)
        this.eventCooldowns.put(playerId, System.currentTimeMillis() + (this.plugin.getConfigManager().getPluginConfig().getEventCooldownSeconds() * 1000L));
    }

    private boolean isOnEventCooldown(final UUID playerId) {
        final Long cooldownEnd = this.eventCooldowns.get(playerId);
        return cooldownEnd != null && System.currentTimeMillis() < cooldownEnd;
    }

    /**
     * Summons mobs for the given stage, registers them and starts teleport/task logic.
     */
    public void summonMobsForStage(final Player player, final int stage) {
        final Location spawnLocation = player.getLocation().add(0, 1, 0);
        final PluginConfig config = this.plugin.getConfigManager().getPluginConfig();
        final UUID playerId = player.getUniqueId();

        // If event is not active for player, set it active (defensive)
        if (!Boolean.TRUE.equals(this.eventActive.get(playerId))) {
            this.eventActive.put(playerId, true);
        }

        // Clear existing mobs (should be dead/cleared)
        this.clearPlayerMobs(playerId);

        // update current stage tracking
        this.playerCurrentStage.put(playerId, stage);
        final PlayerData pd = this.playerDataManager.getPlayerData(playerId);
        if (pd != null) {
            pd.setCurrentEventStage(stage);
            pd.setEventStageXP(0);
        }

        final List<LivingEntity> newMobs = new ArrayList<>();

        switch (stage) {
            case 1 -> newMobs.addAll(this.summonShadowHounds(spawnLocation, player, config));
            case 2 -> newMobs.addAll(this.summonBlackParrots(spawnLocation, player, config));
            case 3 -> newMobs.addAll(this.summonKillerRabbits(spawnLocation, player, config));
            case 4 -> newMobs.addAll(this.summonGiantPanda(spawnLocation, player, config));
            case 5 -> newMobs.addAll(this.summonShadowWither(spawnLocation, player, config));
            default -> { /* no-op for unknown stage */ }
        }

        // Register all mobs with owner and metadata
        for (final LivingEntity mob : newMobs) {
            this.mobOwnerMap.put(mob.getUniqueId(), playerId);
            mob.getPersistentDataContainer().set(this.eventOwnerKey, PersistentDataType.STRING, playerId.toString());
            mob.getPersistentDataContainer().set(this.eventSummonedKey, PersistentDataType.BOOLEAN, true);
        }

        // Store the list even if empty (to track state)
        this.playerSummonedMobs.put(playerId, newMobs);

        // Play spawn particles
        this.plugin.getParticleManager().playEventMobSpawn(spawnLocation);

        // Start mob teleportation task (if mobs exist)
        if (!newMobs.isEmpty()) {
            this.startMobTeleportationTask(player, newMobs);
        }

        // Send stage message
        player.sendMessage(Component.text("§7Stage §e" + stage + " §7— " + this.getStageName(stage) + " §7summoned!", NamedTextColor.GRAY));
    }

    private void startMobTeleportationTask(final Player owner, final List<LivingEntity> mobs) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!owner.isOnline()) {
                    // Owner logged out -> despawn all their mobs immediately (per spec)
                    mobs.forEach(m -> {
                        if (m != null && !m.isDead()) {
                            mobOwnerMap.remove(m.getUniqueId());
                            m.remove();
                        }
                    });
                    mobs.clear();
                    playerSummonedMobs.remove(owner.getUniqueId());
                    this.cancel();
                    return;
                }

                if (mobs.isEmpty() || mobs.stream().allMatch(LivingEntity::isDead)) {
                    this.cancel();
                    return;
                }

                final Location ownerLocation = owner.getLocation();

                for (final LivingEntity mob : new ArrayList<>(mobs)) {
                    if (mob == null || mob.isDead()) continue;

                    final double distance = mob.getLocation().distance(ownerLocation);

                    // Teleport if mob is too far away (>30 blocks) or stuck
                    if (distance > 30.0 || (mob.getVelocity().lengthSquared() < 0.01 && distance > 10.0)) {
                        final Location teleportLocation = ownerLocation.clone().add(
                                (Math.random() - 0.5) * 8,
                                1,
                                (Math.random() - 0.5) * 8
                        );

                        mob.teleport(teleportLocation);

                        // Re-target nearest untrusted player
                        final Player nearestTarget = MobProgressionManager.this.plugin.getTrustManager().findNearestUntrustedPlayer(owner, teleportLocation);
                        if (nearestTarget != null && mob instanceof Creature creature) {
                            creature.setTarget(nearestTarget);
                        }
                    }
                }
            }
        }.runTaskTimer(this.plugin, 100L, 100L); // Every 5 seconds
    }

    private String getStageName(final int stage) {
        return switch (stage) {
            case 1 -> "Hounds of the Abyss";
            case 2 -> "Black Parrot Swarm";
            case 3 -> "Killer Rabbits";
            case 4 -> "Berserk Panda";
            case 5 -> "Shadow Wither";
            default -> "Unknown Stage";
        };
    }

    // --- Summon helpers: always add the mob to the list; only set target if nearestTarget exists ---

    private List<LivingEntity> summonShadowHounds(final Location location, final Player owner, final PluginConfig config) {
        final List<LivingEntity> hounds = new ArrayList<>();
        final int spawnCount = config.getHoundSpawnCount();

        for (int i = 0; i < spawnCount; i++) {
            final Location spawnLoc = location.clone().add(
                    (Math.random() - 0.5) * 4,
                    0,
                    (Math.random() - 0.5) * 4
            );

            final Wolf wolf = (Wolf) location.getWorld().spawnEntity(spawnLoc, EntityType.WOLF);
            wolf.setOwner(owner);
            wolf.setAngry(true);

            // Apply configured stats
            if (wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(config.getHoundHealth());
            }
            wolf.setHealth(Math.min(wolf.getMaxHealth(), config.getHoundHealth()));
            if (wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
                wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(config.getHoundDamage());
            }

            // Apply buffs
            wolf.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));
            wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
            wolf.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));

            wolf.setCustomName("§4Hound of the Abyss");
            wolf.setCustomNameVisible(true);

            // Schedule despawn after lifetime
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!wolf.isDead()) {
                        mobOwnerMap.remove(wolf.getUniqueId());
                        wolf.remove();
                    }
                }
            }.runTaskLater(MobProgressionManager.this.plugin, config.getHoundLifetime() * 20L);

            // Start particle effects
            MobProgressionManager.this.startHoundParticles(wolf, config.getHoundParticleDensity());

            // Try to target nearest untrusted player if exists
            final Player nearestTarget = this.plugin.getTrustManager().findNearestUntrustedPlayer(owner, spawnLoc);
            if (nearestTarget != null && wolf instanceof Creature creature) {
                creature.setTarget(nearestTarget);
            }

            hounds.add(wolf);
        }

        return hounds;
    }

    private List<LivingEntity> summonBlackParrots(final Location location, final Player owner, final PluginConfig config) {
        final List<LivingEntity> parrots = new ArrayList<>();
        final int spawnCount = config.getParrotSpawnCount();

        for (int i = 0; i < spawnCount; i++) {
            final Location spawnLoc = location.clone().add(
                    (Math.random() - 0.5) * 6,
                    Math.random() * 3 + 2,
                    (Math.random() - 0.5) * 6
            );

            final Parrot parrot = (Parrot) location.getWorld().spawnEntity(spawnLoc, EntityType.PARROT);
            parrot.setVariant(Parrot.Variant.GRAY);

            // Apply configured stats
            if (parrot.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                parrot.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(config.getParrotHealth());
            }
            parrot.setHealth(Math.min(parrot.getMaxHealth(), config.getParrotHealth()));
            if (parrot.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
                parrot.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(config.getParrotDamage());
            }

            parrot.setCustomName("§5Black Parrot");
            parrot.setCustomNameVisible(true);

            // Add poison attack capability
            this.startPoisonAttack(parrot, owner, config.getParrotPoisonDuration());

            // Start particle effects
            this.startParrotParticles(parrot, config.getParrotParticleDensity());

            // Try targeting nearest untrusted player
            final Player nearestTarget = this.plugin.getTrustManager().findNearestUntrustedPlayer(owner, spawnLoc);
            if (nearestTarget != null && parrot instanceof Creature creature) {
                creature.setTarget(nearestTarget);
            }

            parrots.add(parrot);
        }

        return parrots;
    }

    private List<LivingEntity> summonKillerRabbits(final Location location, final Player owner, final PluginConfig config) {
        final List<LivingEntity> rabbits = new ArrayList<>();
        final int spawnCount = config.getRabbitSpawnCount();

        for (int i = 0; i < spawnCount; i++) {
            final Location spawnLoc = location.clone().add(
                    (Math.random() - 0.5) * 8,
                    0,
                    (Math.random() - 0.5) * 8
            );

            final Rabbit rabbit = (Rabbit) location.getWorld().spawnEntity(spawnLoc, EntityType.RABBIT);
            rabbit.setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);

            // Apply configured stats
            if (rabbit.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                rabbit.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(config.getRabbitHealth());
            }
            rabbit.setHealth(Math.min(rabbit.getMaxHealth(), config.getRabbitHealth()));
            if (rabbit.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
                rabbit.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(config.getRabbitDamage());
            }
            if (rabbit.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
                rabbit.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.5);
            }

            rabbit.setCustomName("§cKiller Rabbit");
            rabbit.setCustomNameVisible(true);

            // Start particle effects
            this.startRabbitParticles(rabbit, config.getRabbitParticleDensity());

            // Try targeting nearest untrusted player
            final Player nearestTarget = this.plugin.getTrustManager().findNearestUntrustedPlayer(owner, spawnLoc);
            if (nearestTarget != null && rabbit instanceof Creature creature) {
                creature.setTarget(nearestTarget);
            }

            rabbits.add(rabbit);
        }

        return rabbits;
    }

    private List<LivingEntity> summonGiantPanda(final Location location, final Player owner, final PluginConfig config) {
        final List<LivingEntity> pandas = new ArrayList<>();

        final Location spawnLoc = location.clone().add(0, 0, 2);
        final Panda panda = (Panda) location.getWorld().spawnEntity(spawnLoc, EntityType.PANDA);

        panda.setMainGene(Panda.Gene.AGGRESSIVE);
        panda.setHiddenGene(Panda.Gene.AGGRESSIVE);

        // Apply configured stats
        if (panda.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            panda.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(config.getPandaHealth());
        }
        panda.setHealth(Math.min(panda.getMaxHealth(), config.getPandaHealth()));
        if (panda.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            panda.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(config.getPandaDamage());
        }
        if (panda.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            panda.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.4);
        }
        if (panda.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null) {
            panda.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(config.getPandaKnockbackPower());
        }

        panda.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, config.getPandaResistanceLevel()));
        panda.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 2));

        panda.setCustomName("§6Shadow Panda(he likes Phantom)");
        panda.setCustomNameVisible(true);

        // Start particle effects
        this.startPandaParticles(panda, config.getPandaParticleDensity());

        // Try target nearest untrusted player
        final Player nearestTarget = this.plugin.getTrustManager().findNearestUntrustedPlayer(owner, spawnLoc);
        if (nearestTarget != null && panda instanceof Creature creature) {
            creature.setTarget(nearestTarget);
        }

        pandas.add(panda);
        return pandas;
    }

    /**
     * Summon the Shadow Wither.
     *
     * Key differences from earlier:
     * - Keep Wither native AI (we DO NOT disable AI or replace movement).
     * - Add a periodic task to ensure it stays near the owner (10 block radius).
     * - Intercept its skull projectiles elsewhere to prevent explosions and adjust damage.
     * - Hide bossbar using Bukkit Boss API (Paper 1.21 compatible).
     */
    private List<LivingEntity> summonShadowWither(final Location location, final Player owner, final PluginConfig config) {
        final List<LivingEntity> withers = new ArrayList<>();

        final Location spawnLoc = location.clone().add(0, 0, 3);
        final Wither wither = (Wither) Objects.requireNonNull(location.getWorld()).spawnEntity(spawnLoc, EntityType.WITHER);

        // DON'T disable AI: keep vanilla movement & targeting, but we intercept targeting/damage elsewhere.
        // wither.setAI(false); <-- removed as per request to keep vanilla AI

        // Apply configured stats: plugin config uses wither fields
        if (wither.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            wither.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(config.getWitherHealth());
        }
        wither.setHealth(Math.min(wither.getMaxHealth(), config.getWitherHealth()));

        // Name and visuals
        wither.setCustomName("§8§lShadow Wither");
        wither.setCustomNameVisible(true);

        // Hide bossbar on Paper 1.21 using Boss API: remove all players from the bar repeatedly
        attemptHideWitherBossbar(wither);

        // Start a keeper task that ensures Wither remains near owner (teleport if >10 blocks).
        startWitherKeeperTask(wither, owner, config);

        // Start particle effects
        this.startWitherParticles(wither, config.getWitherParticleDensity());

        withers.add(wither);
        return withers;
    }

    /**
     * Hide the Wither bossbar using the Bukkit Boss API.
     *
     * This avoids fragile NMS and works on Paper 1.21: we remove all players from the Wither's BossBar
     * repeatedly while the Wither exists so players don't see the bar.
     */
    private void attemptHideWitherBossbar(final Wither wither) {
        // Only proceed if entity implements Boss (Wither does)
        if (!(wither instanceof Boss bossEntity)) return;

        final BossBar bar = bossEntity.getBossBar();
        if (bar == null) return;

        // Run a repeating task to ensure players are not shown the bossbar.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (wither == null || wither.isDead()) {
                    // clean up and stop
                    this.cancel();
                    return;
                }

                try {
                    // Remove all players from the bossbar to hide it.
                    bar.removeAll();

                    // Also, in case some players are re-added by server, explicitly remove any player in bar.getPlayers()
                    for (Player p : new ArrayList<>(bar.getPlayers())) {
                        bar.removePlayer(p);
                    }
                } catch (Throwable ignored) {
                    // If something unexpected occurs, keep running but ignore the exception (defensive)
                }
            }
        }.runTaskTimer(this.plugin, 2L, 20L); // every 1 second
    }

    private void startWitherKeeperTask(final Wither wither, final Player owner, final PluginConfig config) {
        final UUID ownerId = owner.getUniqueId();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (wither == null || wither.isDead()) {
                    this.cancel();
                    return;
                }

                if (!owner.isOnline()) {
                    // Owner offline -> remove wither (as requested).
                    if (!wither.isDead()) {
                        mobOwnerMap.remove(wither.getUniqueId());
                        wither.remove();
                    }
                    this.cancel();
                    return;
                }

                final Location ownerLoc = owner.getLocation();
                final Location witherLoc = wither.getLocation();

                // If wither is too far ( > 10 blocks ), teleport it near owner.
                final double maxAllowed = Math.max(10.0, 64.0); // Wither will follow up to 64 blocks
                double distance = witherLoc.distance(ownerLoc);
                if (distance > maxAllowed) {
                    final Location tp = ownerLoc.clone().add((Math.random() - 0.5) * 6, 1.0, (Math.random() - 0.5) * 6);
                    wither.teleport(tp);
                    // Ensure it doesn't immediately target owner or trusted players: retarget logic will handle that.
                }

                // Keep it from despawning by accident; ensure persistent metadata stays set (in case of reloads)
                wither.getPersistentDataContainer().set(eventSummonedKey, PersistentDataType.BOOLEAN, true);
                wither.getPersistentDataContainer().set(eventOwnerKey, PersistentDataType.STRING, ownerId.toString());
            }
        }.runTaskTimer(this.plugin, 20L, 40L); // check every 2 seconds
    }

    private void startWitherParticles(final Wither wither, final int density) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (wither.isDead()) {
                    this.cancel();
                    return;
                }

                final Location loc = wither.getLocation().add(0, 1.5, 0);
                wither.getWorld().spawnParticle(org.bukkit.Particle.LARGE_SMOKE, loc, Math.max(4, density / 3), 0.5, 0.5, 0.5, 0.02);
                wither.getWorld().spawnParticle(org.bukkit.Particle.WITCH, loc, Math.max(6, density / 2), 0.4, 0.4, 0.4, 0.01);
            }
        }.runTaskTimer(this.plugin, 0L, 6L);
    }

    private void startHoundParticles(final Wolf hound, final int density) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (hound.isDead()) {
                    this.cancel();
                    return;
                }

                final Location loc = hound.getLocation().add(0, 0.5, 0);
                hound.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, loc, Math.max(1, density / 4), 0.3, 0.3, 0.3, 0.02);
                hound.getWorld().spawnParticle(org.bukkit.Particle.SOUL, loc, Math.max(1, density / 8), 0.2, 0.2, 0.2, 0.01);
            }
        }.runTaskTimer(this.plugin, 0L, 5L);
    }

    private void startParrotParticles(final Parrot parrot, final int density) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (parrot.isDead()) {
                    this.cancel();
                    return;
                }

                final Location loc = parrot.getLocation();
                parrot.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, loc, Math.max(1, density / 6), 0.2, 0.2, 0.2, 0.01);
                parrot.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, loc, Math.max(1, density / 8), 0.1, 0.1, 0.1, 0.01);
            }
        }.runTaskTimer(this.plugin, 0L, 3L);
    }

    private void startRabbitParticles(final Rabbit rabbit, final int density) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (rabbit.isDead()) {
                    this.cancel();
                    return;
                }

                final Location loc = rabbit.getLocation().add(0, 0.2, 0);
                rabbit.getWorld().spawnParticle(org.bukkit.Particle.CRIT, loc, Math.max(1, density / 4), 0.3, 0.1, 0.3, 0.1);
            }
        }.runTaskTimer(this.plugin, 0L, 10L);
    }

    private void startPandaParticles(final Panda panda, final int density) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (panda.isDead()) {
                    this.cancel();
                    return;
                }

                final Location loc = panda.getLocation().add(0, 1, 0);
                panda.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, loc, Math.max(1, density / 3), 0.5, 0.5, 0.5, 0.05);
                panda.getWorld().spawnParticle(org.bukkit.Particle.BLOCK, loc, Math.max(1, density / 4), 0.3, 0.1, 0.3, 0.1, Material.BLACKSTONE.createBlockData());
            }
        }.runTaskTimer(this.plugin, 0L, 8L);
    }

    private void startPoisonAttack(final Parrot parrot, final Player owner, final int poisonDuration) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (parrot.isDead()) {
                    this.cancel();
                    return;
                }

                final LivingEntity target = parrot instanceof Creature creature ? creature.getTarget() : null;
                if (target instanceof Player) {
                    final Player targetPlayer = (Player) target;
                    if (!MobProgressionManager.this.plugin.getTrustManager().canHarm(owner, targetPlayer)) {
                        return; // Skip trusted players
                    }

                    if (parrot.getLocation().distance(target.getLocation()) < 3) {
                        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, poisonDuration * 20, 1));
                        MobProgressionManager.this.plugin.getParticleManager().playPoisonEffect(targetPlayer.getLocation());
                    }
                }
            }
        }.runTaskTimer(this.plugin, 0L, 40L); // Every 2 seconds
    }

    @EventHandler
    public void onEntityTarget(final EntityTargetLivingEntityEvent event) {
        final Entity entity = event.getEntity();

        // Check if this is an event-summoned mob
        if (!entity.getPersistentDataContainer().has(this.eventSummonedKey, PersistentDataType.BOOLEAN)) {
            return;
        }

        final LivingEntity target = event.getTarget();
        if (!(target instanceof Player targetPlayer)) {
            return;
        }

        // Get the owner of this mob
        final String ownerIdString = entity.getPersistentDataContainer().get(this.eventOwnerKey, PersistentDataType.STRING);
        if (ownerIdString == null) {
            return;
        }

        final UUID ownerId = UUID.fromString(ownerIdString);
        final Player owner = this.plugin.getServer().getPlayer(ownerId);
        if (owner == null) {
            return;
        }

        // Cancel targeting if target is owner or trusted
        if (targetPlayer.equals(owner) || this.plugin.getTrustManager().canHelp(owner, targetPlayer)) {
            event.setCancelled(true);

            // Retarget to nearest untrusted player
            Player newTarget = this.plugin.getTrustManager().findNearestUntrustedPlayer(owner, entity.getLocation());
            if (newTarget != null && entity instanceof Creature creature) {
                creature.setTarget(newTarget);
            }
        }
    }

    /**
     * Handle Wither skull projectile hits:
     * - No block explosions / griefing
     * - Visual explosion effects only
     * - If skull hits an entity and is from a summoned Wither, apply true damage (4 HP) to untrusted targets only
     */
    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent event) {
        final Projectile proj = event.getEntity();

        // Only intercept WitherSkull projectiles
        if (!(proj instanceof WitherSkull)) {
            return;
        }

        // Determine shooter
        final ProjectileSource shooter = proj.getShooter();
        if (!(shooter instanceof Wither shooterWither)) {
            return;
        }

        // Is this wither an event-summoned mob?
        if (!shooterWither.getPersistentDataContainer().has(this.eventSummonedKey, PersistentDataType.BOOLEAN)) {
            return;
        }

        // Prevent any default explosion or block damage: we will only show visuals
        final Location hitLocation = proj.getLocation();

        // Visual explosion (smoke + sound)
        hitLocation.getWorld().spawnParticle(org.bukkit.Particle.LARGE_SMOKE, hitLocation, 6, 0.6, 0.6, 0.6, 0.01);
        hitLocation.getWorld().playSound(hitLocation, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.0f);

        // If projectile hit an entity, the actual EntityDamageByEntityEvent may or may not be fired by server before/after this.
        // To guarantee precise behaviour: we will detect nearby entity hits in a small radius and apply true damage
        // of 4 HP to the nearest valid target (prefer direct collision).
        final List<Entity> nearby = hitLocation.getWorld().getNearbyEntities(hitLocation, 1.2, 1.2, 1.2).stream().toList();

        Entity directHit = null;
        for (Entity e : nearby) {
            if (e == shooterWither || e.equals(proj)) continue;
            if (e instanceof LivingEntity) {
                directHit = e;
                break;
            }
        }

        if (directHit instanceof LivingEntity livingTarget) {
            // Get owner
            final String ownerIdString = shooterWither.getPersistentDataContainer().get(this.eventOwnerKey, PersistentDataType.STRING);
            if (ownerIdString == null) {
                // fallback: do nothing
            } else {
                final UUID ownerId = UUID.fromString(ownerIdString);
                final Player owner = this.plugin.getServer().getPlayer(ownerId);
                boolean allowed = true;
                if (livingTarget instanceof Player targetPlayer && owner != null) {
                    // Don't damage owner or trusted
                    if (targetPlayer.equals(owner) || this.plugin.getTrustManager().canHelp(owner, targetPlayer)) {
                        allowed = false;
                    }
                }

                if (allowed) {
                    // Apply "true" damage of 4.0 (bypasses armor/resistance) by reducing health directly.
                    double newHp = Math.max(0.0, livingTarget.getHealth() - 4.0);
                    livingTarget.setHealth(newHp);

                    // Record last damager for mastery/credit (owner gets credit)
                    if (owner != null) {
                        this.lastDamagerMap.put(livingTarget.getUniqueId(), owner.getUniqueId());
                    }

                    // play shadow beam particle between wither eye and target (if available)
                    try {
                        plugin.getParticleManager().playShadowBeam(shooterWither.getEyeLocation(), livingTarget.getLocation());
                    } catch (Throwable ignored) { }
                }
            }
        }

        // Remove projectile to ensure no further effects
        proj.remove();
    }

    /**
     * Ensure WitherSkull direct damage also sets correct values if server already applied damage
     * (this handler ensures consistent damage when EntityDamageByEntityEvent fires).
     */
    @EventHandler
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        final Entity victim = event.getEntity();
        final Entity damager = event.getDamager();

        // If damager is a WitherSkull (projectile) and shooter is a Wither we control, override damage
        if (damager instanceof WitherSkull skull) {
            final ProjectileSource shooter = skull.getShooter();
            if (shooter instanceof Wither shooterWither) {
                if (shooterWither.getPersistentDataContainer().has(this.eventSummonedKey, PersistentDataType.BOOLEAN)) {

                    // Prevent wither effect and explosion consequences; ensure 4 HP true damage
                    // If the victim is a trusted player or the owner, cancel damage entirely
                    final String ownerIdString = shooterWither.getPersistentDataContainer().get(this.eventOwnerKey, PersistentDataType.STRING);
                    if (ownerIdString != null) {
                        final UUID ownerId = UUID.fromString(ownerIdString);
                        final Player owner = this.plugin.getServer().getPlayer(ownerId);

                        if (victim instanceof Player victimPlayer && owner != null) {
                            if (victimPlayer.equals(owner) || this.plugin.getTrustManager().canHelp(owner, victimPlayer)) {
                                // cancel - never attack owner/trusted players
                                event.setCancelled(true);
                                // retarget the wither to a valid target (if applicable)
                                Player newTarget = this.plugin.getTrustManager().findNearestUntrustedPlayer(owner, shooterWither.getLocation());
                                if (newTarget != null && shooterWither instanceof Creature creature) {
                                    creature.setTarget(newTarget);
                                }
                                return;
                            }
                        }

                        // If not cancelled, set damage to 4 HP by directly reducing health.
                        // If not cancelled, apply normal Bukkit damage (respects armor, Protection, etc.)
if (victim instanceof LivingEntity living) {
    event.setCancelled(false); // let Bukkit handle damage normally
    event.setDamage(8.0); // 8 damage = 4 hearts base, armor will reduce it


                            // record last damager for mastery credit (owner gets credit)
                            if (owner != null) {
                                this.lastDamagerMap.put(living.getUniqueId(), owner.getUniqueId());
                            }

                            // play particles
                            try {
                                plugin.getParticleManager().playShadowBeam(shooterWither.getEyeLocation(), living.getLocation());
                            } catch (Throwable ignored) { }
                        } else {
                            // not a living entity -> cancel damage to prevent block grief/explosion side-effects
                            event.setCancelled(true);
                        }

                    }
                }
            }
        }

        // Check if victim is an event-summoned mob: handle owner hitting to retarget
        if (victim.getPersistentDataContainer().has(this.eventSummonedKey, PersistentDataType.BOOLEAN)) {
            if (damager instanceof Player damagerPlayer) {
                final String ownerIdString = victim.getPersistentDataContainer().get(this.eventOwnerKey, PersistentDataType.STRING);
                if (ownerIdString != null) {
                    final UUID ownerId = UUID.fromString(ownerIdString);
                    final Player owner = this.plugin.getServer().getPlayer(ownerId);
                    if (owner != null) {
                        // Allow owner/trusted to damage but prevent retaliation: retarget to nearest untrusted player
                        if (damagerPlayer.equals(owner) || this.plugin.getTrustManager().canHelp(owner, damagerPlayer)) {
                            if (victim instanceof Creature creature) {
                                Player newTarget = this.plugin.getTrustManager().findNearestUntrustedPlayer(owner, victim.getLocation());
                                creature.setTarget(newTarget);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent event) {
        final LivingEntity entity = event.getEntity();
        final Player killer = entity.getKiller();

        // Track last damager for mastery credit
        if (killer != null) {
            this.lastDamagerMap.put(entity.getUniqueId(), killer.getUniqueId());
        }

        // Check if this entity was summoned by an event mask user
        final UUID ownerUUID = this.mobOwnerMap.get(entity.getUniqueId());
        if (ownerUUID == null) {
            return;
        }

        final Player owner = this.plugin.getServer().getPlayer(ownerUUID);
        if (owner == null) {
            // owner offline -> cleanup mapping and return
            this.mobOwnerMap.remove(entity.getUniqueId());
            return;
        }

        // Remove from mob tracking
        this.mobOwnerMap.remove(entity.getUniqueId());
        final List<LivingEntity> ownerMobs = this.playerSummonedMobs.get(ownerUUID);
        if (ownerMobs != null) {
            ownerMobs.remove(entity);
        }

        // Handle Shadow Wither mastery reward
        if (entity instanceof Wither && entity.getCustomName() != null &&
                entity.getCustomName().contains("Shadow Wither")) {
            this.handleWitherMastery(entity, owner);
        }

        // Re-check the owner's mob list: if no mobs remain -> progress
        final List<LivingEntity> tracked = this.playerSummonedMobs.get(ownerUUID);
        final boolean noMobsLeft = (tracked == null) ||
                tracked.isEmpty() ||
                tracked.stream().allMatch(LivingEntity::isDead);

        if (noMobsLeft) {
            final Integer currentStage = this.playerCurrentStage.get(ownerUUID);
            if (currentStage == null) return;

            if (currentStage < 5) {
                // Advance to next stage with delay
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        final int nextStage = currentStage + 1;
                        playerCurrentStage.put(ownerUUID, nextStage);

                        final PlayerData ownerData = playerDataManager.getPlayerData(ownerUUID);
                        if (ownerData != null) {
                            ownerData.setCurrentEventStage(nextStage);
                            ownerData.setEventStageXP(0);
                        }

                        // Clear any lingering dead mobs and summon next stage
                        clearPlayerMobs(ownerUUID);

                        final Player p = plugin.getServer().getPlayer(ownerUUID);
                        if (p != null) {
                            summonMobsForStage(p, nextStage);
                            // Send wave cleared message
                            p.sendMessage(Component.text("§7Stage §e" + currentStage + " §7cleared! Next stage in §e4s.", NamedTextColor.GRAY));
                        }

                        // Save progression
                        playerDataManager.savePlayerData(ownerUUID);
                    }
                }.runTaskLater(this.plugin, 80L); // 4 second delay
            } else {
                // Stage 5 completed - reset to allow restart at Stage 1 (event ends)
                this.playerCurrentStage.put(ownerUUID, 1);
                final PlayerData ownerData = this.playerDataManager.getPlayerData(ownerUUID);
                if (ownerData != null) {
                    ownerData.setCurrentEventStage(1);
                    ownerData.setEventStageXP(0);
                }

                // Send completion message to owner
                owner.sendMessage(Component.text("§7The Shadow Wither has fallen.", NamedTextColor.GRAY));

                // Mark event inactive so manual re-activation is required
                this.eventActive.remove(ownerUUID);

                this.playerDataManager.savePlayerData(ownerUUID);
            }
        }
    }

    private void handleWitherMastery(final LivingEntity wither, final Player owner) {
        // Determine who gets the mastery credit
        Player masteryRecipient = wither.getKiller();

        // Fallback to last damager if no direct killer
        if (masteryRecipient == null) {
            final UUID lastDamagerUUID = this.lastDamagerMap.get(wither.getUniqueId());
            if (lastDamagerUUID != null) {
                masteryRecipient = this.plugin.getServer().getPlayer(lastDamagerUUID);
            }
        }

        // Final fallback to owner
        if (masteryRecipient == null) {
            masteryRecipient = owner;
        }

        // Award mastery to the recipient (private message only)
        if (masteryRecipient != null) {
            masteryRecipient.sendMessage(Component.text("§6§l[Mastery] §7You have mastered the Forbidden Shadows! §aYou receive a Totem of Undying.", NamedTextColor.GOLD));

            // Give Totem of Undying
            final ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
            if (masteryRecipient.getInventory().firstEmpty() != -1) {
                masteryRecipient.getInventory().addItem(totem);
            } else {
                // Drop with pickup protection
                final org.bukkit.entity.Item droppedTotem = masteryRecipient.getWorld().dropItem(masteryRecipient.getLocation(), totem);
                droppedTotem.setPickupDelay(0);
                droppedTotem.setOwner(masteryRecipient.getUniqueId());
            }

            // Play celebratory particles only around the killer
            this.plugin.getParticleManager().playEventActivationParticles(masteryRecipient.getLocation());
            masteryRecipient.getWorld().spawnParticle(org.bukkit.Particle.FIREWORK,
                    masteryRecipient.getLocation().add(0, 2, 0), 10, 1, 1, 1, 0.1);
        }

        // Clean up tracking
        this.lastDamagerMap.remove(wither.getUniqueId());
    }

    public void clearPlayerMobs(final UUID playerId) {
        final List<LivingEntity> mobs = this.playerSummonedMobs.get(playerId);
        if (mobs != null) {
            for (final LivingEntity mob : new ArrayList<>(mobs)) {
                if (mob != null && !mob.isDead()) {
                    this.mobOwnerMap.remove(mob.getUniqueId());
                    mob.remove();
                }
            }
            mobs.clear();
        }
        // also remove the list reference to avoid stale lists
        this.playerSummonedMobs.remove(playerId);
    }

    public void clearAllMobs() {
        for (final UUID playerId : new HashSet<>(this.playerSummonedMobs.keySet())) {
            this.clearPlayerMobs(playerId);
        }
        this.playerSummonedMobs.clear();
        this.playerCurrentStage.clear();
        this.mobOwnerMap.clear();
        this.lastDamagerMap.clear();
        this.eventActive.clear();
    }

    public PlayerDataManager getPlayerDataManager() {
        return this.playerDataManager;
    }

    public int getPlayerCurrentStage(UUID playerId) {
        return this.playerCurrentStage.getOrDefault(playerId, 1);
    }

    public void setPlayerCurrentStage(UUID playerId, int stage) {
        this.playerCurrentStage.put(playerId, stage);
    }

    // --- Player join/quit to support despawn on logout and respawn on login ---

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player p = event.getPlayer();
        final UUID playerId = p.getUniqueId();

        // Despawn any summoned mobs owned by this player but keep stage info so they can respawn on join
        final List<LivingEntity> mobs = this.playerSummonedMobs.get(playerId);
        if (mobs != null) {
            for (LivingEntity m : new ArrayList<>(mobs)) {
                if (m != null && !m.isDead()) {
                    mobOwnerMap.remove(m.getUniqueId());
                    m.remove();
                }
            }
            mobs.clear();
            this.playerSummonedMobs.remove(playerId);
        }

        // Note: do not clear playerCurrentStage or eventActive here; stage should persist so on join we can resume
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        final UUID playerId = p.getUniqueId();

        // If the player previously had an active event and was at Stage 5 (Wither), respawn the wither near them
        if (Boolean.TRUE.equals(this.eventActive.get(playerId))) {
            final Integer stage = this.playerCurrentStage.get(playerId);
            if (stage != null && stage == 5) {
                // only spawn if no wither currently present for them
                final List<LivingEntity> current = this.playerSummonedMobs.get(playerId);
                boolean hasWither = false;
                if (current != null) {
                    for (LivingEntity le : current) {
                        if (le instanceof Wither && !le.isDead()) {
                            hasWither = true;
                            break;
                        }
                    }
                }
                if (!hasWither) {
                    // summon stage 5 wither near them
                    summonMobsForStage(p, 5);
                }
            }
        }
    }
}
