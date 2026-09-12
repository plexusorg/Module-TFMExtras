package dev.plex.extras;

import org.bukkit.Bukkit;

import dev.plex.api.config.ModuleConfiguration;
import dev.plex.extras.command.AdminInfoCommand;
import dev.plex.extras.command.AutoClearCommand;
import dev.plex.extras.command.AutoTeleportCommand;
import dev.plex.extras.command.CageCommand;
import dev.plex.extras.command.CakeCommand;
import dev.plex.extras.command.CartSitCommand;
import dev.plex.extras.command.ClearChatCommand;
import dev.plex.extras.command.ClownfishCommand;
import dev.plex.extras.command.CloudClearCommand;
import dev.plex.extras.command.CookieCommand;
import dev.plex.extras.command.DiscoCommand;
import dev.plex.extras.command.EjectCommand;
import dev.plex.extras.command.EffectCommand;
import dev.plex.extras.command.EnchantCommand;
import dev.plex.extras.command.EnglishMfCommand;
import dev.plex.extras.command.ExpelCommand;
import dev.plex.extras.command.GravityCommand;
import dev.plex.extras.command.JumpPadsCommand;
import dev.plex.extras.command.OrbitCommand;
import dev.plex.extras.command.PaintballCommand;
import dev.plex.extras.command.RandomFishCommand;
import dev.plex.extras.command.RocketCommand;
import dev.plex.extras.command.SizeCommand;
import dev.plex.extras.command.TrailCommand;
import dev.plex.extras.fun.Cages;
import dev.plex.extras.fun.Disco;
import dev.plex.extras.fun.Paintball;
import dev.plex.extras.fun.Rockets;
import dev.plex.extras.fun.SessionAttributes;
import dev.plex.extras.fun.TemporaryBlocks;
import dev.plex.extras.fun.Trails;
import dev.plex.extras.jumppads.JumpPads;
import dev.plex.extras.listener.ClownfishListener;
import dev.plex.extras.listener.JumpPadsListener;
import dev.plex.extras.listener.OrbitEffectListener;
import dev.plex.extras.listener.PlayerListener;
import dev.plex.module.PlexModule;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public class TFMExtras extends PlexModule
{
    @Getter
    private JumpPads jumpPads;

    @Getter
    private ModuleConfiguration config;
    private final Map<UUID, Integer> orbitStrengths = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> orbitTasks = new ConcurrentHashMap<>();
    private ExecutorService configExecutor;
    private TemporaryBlocks temporaryBlocks;
    private Trails trails;
    private Paintball paintball;
    private Cages cages;
    private Disco disco;
    private Rockets rockets;
    private SessionAttributes sessionAttributes;

    @Override
    public void load()
    {
        config = api().moduleConfigs().create(this, "config.yml");
        config.load();
        loadMessages("messages.yml");
        jumpPads = new JumpPads(config.getInt("server.jumppad_strength", 1));
        temporaryBlocks = new TemporaryBlocks(this);
        trails = new Trails(this, temporaryBlocks);
        paintball = new Paintball(this, temporaryBlocks);
        cages = new Cages(this, temporaryBlocks);
        disco = new Disco(this, temporaryBlocks);
        rockets = new Rockets(this);
        sessionAttributes = new SessionAttributes(this);
        registerCommand(new AdminInfoCommand(this));
        registerCommand(new AutoClearCommand(this));
        registerCommand(new AutoTeleportCommand(this));
        registerCommand(new CageCommand(cages));
        registerCommand(new CakeCommand());
        registerCommand(new CartSitCommand());
        registerCommand(new ClearChatCommand());
        registerCommand(new ClownfishCommand(this));
        registerCommand(new CloudClearCommand());
        registerCommand(new CookieCommand());
        registerCommand(new DiscoCommand(disco));
        registerCommand(new EjectCommand());
        registerCommand(new EffectCommand());
        registerCommand(new EnchantCommand());
        registerCommand(new EnglishMfCommand());
        registerCommand(new ExpelCommand());
        registerCommand(new GravityCommand(sessionAttributes));
        registerCommand(new JumpPadsCommand(this));
        registerCommand(new OrbitCommand(this));
        registerCommand(new PaintballCommand(paintball));
        registerCommand(new RandomFishCommand());
        registerCommand(new RocketCommand(rockets));
        registerCommand(new SizeCommand(sessionAttributes));
        registerCommand(new TrailCommand(trails));
    }

    @Override
    public void enable()
    {
        configExecutor = Executors.newSingleThreadExecutor(
                Thread.ofPlatform().daemon().name("Plex-TFMExtras-Config").factory());
        registerListener(new ClownfishListener(this));
        registerListener(new JumpPadsListener(this));
        registerListener(new OrbitEffectListener(this));
        registerListener(new PlayerListener(this));
        registerListener(trails);
        registerListener(paintball);
        registerListener(cages);
        registerListener(sessionAttributes);
    }

    @Override
    public void disable()
    {
        orbitTasks.values().forEach(ScheduledTask::cancel);
        orbitTasks.clear();
        orbitStrengths.clear();
        // Restore every temporary block first and unowned, because Plex cancels this module's owned
        // tasks right after disable. The bound keeps unload inside Plex's shutdown budget, and a
        // timeout surfaces as a logged shutdown failure instead of silent success.
        CompletableFuture<Void> blocks = temporaryBlocks.revertAll();
        rockets.cancelAll();
        disco.stopAll();
        cages.uncageAll();
        completeShutdownBeforeClose(CompletableFuture.allOf(blocks, sessionAttributes.resetOnline())
                .orTimeout(5, TimeUnit.SECONDS));
        if (configExecutor != null)
        {
            configExecutor.shutdownNow();
            configExecutor = null;
        }
    }

    public void teleportRandom(Player player)
    {
        World world = player.getWorld();
        double x = ThreadLocalRandom.current().nextDouble(-100000, 100000);
        double z = ThreadLocalRandom.current().nextDouble(-100000, 100000);
        Location region = new Location(world, x, 0, z);
        ownTask(Bukkit.getRegionScheduler().run(plugin(), region, task ->
        {
            double y = world.getHighestBlockYAt((int)x, (int)z) + 1;
            Location target = new Location(world, x, y, z);
            ownTask(player.getScheduler().run(plugin(), ignored -> player.teleportAsync(target), null));
        }));
    }

    public Integer orbitStrength(UUID playerId)
    {
        return orbitStrengths.get(playerId);
    }

    public void clearOrbitStrength(UUID playerId)
    {
        orbitStrengths.remove(playerId);
        ScheduledTask task = orbitTasks.remove(playerId);
        if (task != null) task.cancel();
    }

    public void startOrbit(Player player, int strength)
    {
        ownTask(player.getScheduler().run(plugin(), entityTask ->
        {
            player.setGameMode(GameMode.SURVIVAL);
            applyOrbit(player, strength);
            orbitStrengths.put(player.getUniqueId(), strength);
            ScheduledTask task = ownTask(player.getScheduler().runAtFixedRate(plugin(),
                    ignored -> applyOrbit(player, strength), null, 100L, 100L));
            ScheduledTask previous = orbitTasks.put(player.getUniqueId(), task);
            if (previous != null) previous.cancel();
        }, null));
    }

    private void applyOrbit(Player player, int strength)
    {
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20 * 10, strength, false, false));
    }

    public CompletableFuture<Boolean> toggleConfigEntry(String path, String value)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            List<String> values = config.getStringList(path);
            boolean enabled = !values.remove(value);
            if (enabled) values.add(value);
            config.set(path, values);
            config.save();
            return enabled;
        }, configExecutor);
    }

}
