package dev.plex.extras;

import dev.plex.api.config.ModuleConfiguration;
import dev.plex.extras.command.AdminInfoCommand;
import dev.plex.extras.command.AutoClearCommand;
import dev.plex.extras.command.AutoTeleportCommand;
import dev.plex.extras.command.CakeCommand;
import dev.plex.extras.command.CartSitCommand;
import dev.plex.extras.command.ClearChatCommand;
import dev.plex.extras.command.ClownfishCommand;
import dev.plex.extras.command.CloudClearCommand;
import dev.plex.extras.command.EjectCommand;
import dev.plex.extras.command.EnchantCommand;
import dev.plex.extras.command.EnglishMfCommand;
import dev.plex.extras.command.ExpelCommand;
import dev.plex.extras.command.JumpPadsCommand;
import dev.plex.extras.command.OrbitCommand;
import dev.plex.extras.command.RandomFishCommand;
import dev.plex.extras.jumppads.JumpPads;
import dev.plex.extras.listener.ClownfishListener;
import dev.plex.extras.listener.JumpPadsListener;
import dev.plex.extras.listener.OrbitEffectListener;
import dev.plex.extras.listener.PlayerListener;
import dev.plex.module.PlexModule;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
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
    private final Object configMutationLock = new Object();

    @Override
    public void load()
    {
        config = api().moduleConfigs().create(this, "config.yml");
        config.load();
        loadMessages("messages.yml");
        jumpPads = new JumpPads(config.getInt("server.jumppad_strength", 1));
        registerCommand(new AdminInfoCommand(this));
        registerCommand(new AutoClearCommand(this));
        registerCommand(new AutoTeleportCommand(this));
        registerCommand(new CakeCommand());
        registerCommand(new CartSitCommand());
        registerCommand(new ClearChatCommand());
        registerCommand(new ClownfishCommand(this));
        registerCommand(new CloudClearCommand());
        registerCommand(new EjectCommand());
        registerCommand(new EnchantCommand());
        registerCommand(new EnglishMfCommand());
        registerCommand(new ExpelCommand());
        registerCommand(new JumpPadsCommand(this));
        registerCommand(new OrbitCommand(this));
        registerCommand(new RandomFishCommand());
    }

    @Override
    public void enable()
    {
        registerListener(new ClownfishListener(this));
        registerListener(new JumpPadsListener(this));
        registerListener(new OrbitEffectListener(this));
        registerListener(new PlayerListener(this));
    }

    @Override
    public void disable()
    {
        orbitTasks.values().forEach(ScheduledTask::cancel);
        orbitTasks.clear();
        orbitStrengths.clear();
    }

    public void teleportRandom(Player player)
    {
        World world = player.getWorld();
        double x = ThreadLocalRandom.current().nextDouble(-100000, 100000);
        double z = ThreadLocalRandom.current().nextDouble(-100000, 100000);
        Location region = new Location(world, x, 0, z);
        scheduler().runRegion(region, () ->
        {
            double y = world.getHighestBlockYAt((int)x, (int)z) + 1;
            Location target = new Location(world, x, y, z);
            scheduler().runEntity(player, () -> player.teleportAsync(target));
        });
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
        scheduler().runEntity(player, () ->
        {
            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
            applyOrbit(player, strength);
            orbitStrengths.put(player.getUniqueId(), strength);
            ScheduledTask task = scheduler().runEntityTimer(player, () -> applyOrbit(player, strength), 100L, 100L);
            if (task == null)
            {
                orbitStrengths.remove(player.getUniqueId());
                return;
            }
            ScheduledTask previous = orbitTasks.put(player.getUniqueId(), task);
            if (previous != null) previous.cancel();
        });
    }

    private void applyOrbit(Player player, int strength)
    {
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20 * 10, strength, false, false));
    }

    public CompletableFuture<Boolean> toggleConfigEntry(String path, String value)
    {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        scheduler().runAsync(() ->
        {
            try
            {
                synchronized (configMutationLock)
                {
                    java.util.List<String> values = config.getStringList(path);
                    boolean enabled = !values.remove(value);
                    if (enabled) values.add(value);
                    config.set(path, values);
                    config.save();
                    result.complete(enabled);
                }
            }
            catch (RuntimeException failure)
            {
                result.completeExceptionally(failure);
            }
        });
        return result;
    }

}
