package dev.plex.extras.fun;

import dev.plex.module.PlexModule;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Rockets
{
    private static final int THRUST_TICKS = 40;
    private static final int THRUST_PERIOD = 2;
    private static final int BLAST_INTERVAL = 10;
    private static final double THRUST_VELOCITY = 1.2;
    private static final int SLOW_FALLING_TICKS = 20 * 30;
    private static final List<Color> COLORS = List.of(Color.RED, Color.ORANGE, Color.YELLOW,
            Color.LIME, Color.AQUA, Color.FUCHSIA);

    private final PlexModule module;
    // The reservation is taken before the launch crosses to the player's region; the task map only
    // holds what the flight still needs to cancel.
    private final Set<UUID> flying = ConcurrentHashMap.newKeySet();
    private final Map<UUID, ScheduledTask> flights = new ConcurrentHashMap<>();

    public Rockets(PlexModule module)
    {
        this.module = module;
    }

    public boolean launch(Player player, Runnable done, Runnable retired)
    {
        UUID playerId = player.getUniqueId();
        if (!flying.add(playerId))
        {
            return false;
        }

        Runnable release = () ->
        {
            flying.remove(playerId);
            retired.run();
        };
        EntityOwner.run(module, player, () -> ignite(player, playerId, done, release), release);
        return true;
    }

    public void cancelAll()
    {
        flights.values().forEach(ScheduledTask::cancel);
        flights.clear();
        flying.clear();
    }

    private void land(UUID playerId)
    {
        flights.remove(playerId);
        flying.remove(playerId);
    }

    private void ignite(Player player, UUID playerId, Runnable done, Runnable release)
    {
        if (player.isFlying())
        {
            player.setFlying(false);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 1.0F);

        AtomicInteger elapsed = new AtomicInteger();
        ScheduledTask task = module.ownTask(player.getScheduler().runAtFixedRate(module.plugin(),
                flight -> thrust(player, playerId, flight, elapsed), () -> land(playerId), 1L, THRUST_PERIOD));
        if (task == null)
        {
            release.run();
            return;
        }

        ScheduledTask previous = flights.put(playerId, task);
        if (previous != null)
        {
            previous.cancel();
        }
        done.run();
    }

    private void thrust(Player player, UUID playerId, ScheduledTask task, AtomicInteger elapsed)
    {
        int ticks = elapsed.addAndGet(THRUST_PERIOD);
        player.setVelocity(new Vector(0, THRUST_VELOCITY, 0));
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 10, 0.2, 0.2, 0.2);
        if (ticks % BLAST_INTERVAL == 0)
        {
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.3F, 1.0F);
        }
        if (ticks < THRUST_TICKS)
        {
            return;
        }

        task.cancel();
        detonate(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, SLOW_FALLING_TICKS, 0, false, false));
        land(playerId);
    }

    private void detonate(Player player)
    {
        FireworkEffect effect = FireworkEffect.builder()
                .withColor(COLORS.get(ThreadLocalRandom.current().nextInt(COLORS.size())))
                .with(FireworkEffect.Type.BALL_LARGE)
                .withFlicker()
                .withTrail()
                .build();
        player.getWorld().spawn(player.getLocation(), Firework.class, firework ->
        {
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(effect);
            firework.setFireworkMeta(meta);
            firework.setTicksToDetonate(1);
        });
    }
}
