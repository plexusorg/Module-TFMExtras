package dev.plex.extras.fun;

import dev.plex.extras.TFMExtras;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class Disco
{
    public static final String MAX_SECONDS_PATH = "server.fun.disco_max_seconds";

    private static final List<Sound> NOTES = List.of(
            Sound.BLOCK_NOTE_BLOCK_HARP,
            Sound.BLOCK_NOTE_BLOCK_BASS,
            Sound.BLOCK_NOTE_BLOCK_BELL,
            Sound.BLOCK_NOTE_BLOCK_CHIME,
            Sound.BLOCK_NOTE_BLOCK_PLING);
    private static final int DEFAULT_MAX_SECONDS = 60;
    private static final int BEAT_TICKS = 4;
    private static final int BEATS_PER_SECOND = 5;
    private static final int RADIUS = 3;

    private final TFMExtras module;
    private final TemporaryBlocks blocks;
    private final Map<UUID, Party> parties = new ConcurrentHashMap<>();

    public Disco(TFMExtras module, TemporaryBlocks blocks)
    {
        this.module = module;
        this.blocks = blocks;
    }

    public int maxSeconds()
    {
        return module.getConfig().getInt(MAX_SECONDS_PATH, DEFAULT_MAX_SECONDS);
    }

    public void start(Player target, int seconds, Runnable done, Runnable retired)
    {
        EntityOwner.run(module, target, () -> begin(target, seconds, done, retired), retired);
    }

    public boolean stop(UUID playerId)
    {
        Party party = parties.remove(playerId);
        if (party == null) return false;
        party.task().cancel();
        if (party.floor() != null) party.floor().revert();
        return true;
    }

    public void stopAll()
    {
        parties.keySet().forEach(this::stop);
    }

    // Runs on the target's region, so the player location and the floor blocks are owned here.
    private void begin(Player target, int seconds, Runnable done, Runnable retired)
    {
        UUID playerId = target.getUniqueId();
        stop(playerId);

        Location center = target.getLocation();
        Map<Block, BlockData> colors = floor(center);
        TemporaryBlocks.Placement floor = colors.isEmpty() ? null : blocks.place(center, colors, 0);
        Set<Block> lights = Set.copyOf(colors.keySet());
        AtomicInteger beats = new AtomicInteger();
        // The retired callback runs inside Paper's entity removal, so the floor revert is deferred to
        // the region that owns the floor instead of writing blocks from that critical path. The
        // deferred stop stays unowned because retirement can also happen after the module stopped
        // accepting tasks; stop is idempotent and disable already reverts every floor.
        ScheduledTask task = module.ownTask(target.getScheduler().runAtFixedRate(module.plugin(),
                ignored -> beat(target, floor, lights, beats, seconds),
                () -> Bukkit.getRegionScheduler().run(module.plugin(), center, deferred -> stop(playerId)),
                BEAT_TICKS, BEAT_TICKS));
        if (task == null)
        {
            if (floor != null) floor.revert();
            retired.run();
            return;
        }

        parties.put(playerId, new Party(task, floor));
        done.run();
    }

    private void beat(Player target, @Nullable TemporaryBlocks.Placement floor, Set<Block> lights,
                      AtomicInteger beats, int seconds)
    {
        if (floor != null)
        {
            Map<Block, BlockData> colors = new HashMap<>(lights.size());
            for (Block block : lights)
            {
                colors.put(block, Palette.random().createBlockData());
            }
            floor.repaint(colors);
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        target.getWorld().playSound(target.getLocation(), NOTES.get(random.nextInt(NOTES.size())),
                1.0F, (float)random.nextDouble(0.5, 2.0));

        if (beats.incrementAndGet() >= seconds * BEATS_PER_SECOND)
        {
            stop(target.getUniqueId());
        }
    }

    private Map<Block, BlockData> floor(Location center)
    {
        World world = center.getWorld();
        int cx = center.getBlockX();
        int y = center.getBlockY() - 1;
        int cz = center.getBlockZ();
        Map<Block, BlockData> colors = new HashMap<>();
        for (int dx = -RADIUS; dx <= RADIUS; dx++)
        {
            for (int dz = -RADIUS; dz <= RADIUS; dz++)
            {
                if (dx * dx + dz * dz > RADIUS * RADIUS) continue;
                Block block = world.getBlockAt(cx + dx, y, cz + dz);
                if (!block.getType().isSolid()) continue;
                colors.put(block, Palette.random().createBlockData());
            }
        }
        return colors;
    }

    private record Party(ScheduledTask task, TemporaryBlocks.Placement floor)
    {
    }
}
