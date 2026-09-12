package dev.plex.extras.fun;

import dev.plex.module.PlexModule;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;

public final class TemporaryBlocks
{
    private final PlexModule module;
    private final Map<Block, BlockData> originals = new ConcurrentHashMap<>();
    private final Set<Placement> placements = ConcurrentHashMap.newKeySet();
    private volatile boolean closed;

    public TemporaryBlocks(PlexModule module)
    {
        this.module = module;
    }

    // Paint and restore skip physics so a crop, torch, or fluid next to a temporary block survives
    // the overlay. Placements arriving from another region during disable revert themselves.
    public Placement place(Location anchor, Map<Block, BlockData> blocks, long fadeTicks)
    {
        Map<Block, BlockData> snapshot = Map.copyOf(blocks);
        Placement placement = new Placement(anchor, snapshot.keySet());
        placements.add(placement);
        if (closed)
        {
            placement.revertInternal(false);
            return placement;
        }
        runOwning(anchor, () ->
        {
            if (!placement.active())
            {
                return;
            }
            apply(snapshot);
            if (fadeTicks > 0)
            {
                placement.fadeTask = module.ownTask(Bukkit.getRegionScheduler()
                        .runDelayed(module.plugin(), anchor, task -> placement.revert(), fadeTicks));
            }
        }, true);
        return placement;
    }

    // Module disable runs before Plex cancels the module's owned tasks, so shutdown reverts schedule
    // unowned on the Plex plugin. Region tasks may never run once the server itself is stopping, so
    // the module bounds this result before it holds the classloader open.
    public CompletableFuture<Void> revertAll()
    {
        closed = true;
        CompletableFuture<?>[] completions = placements.stream()
                .map(placement -> placement.revertInternal(false))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(completions);
    }

    // The first placement to revert a block restores it and untracks it. A later placement that
    // covered the same block skips it, so an overlapping placement never writes a stale original.
    // A chunk that unloaded since the paint is loaded again here rather than left painted on disk.
    private void restore(Set<Block> blocks)
    {
        for (Block block : blocks)
        {
            BlockData original = originals.remove(block);
            if (original != null)
            {
                block.setBlockData(original, false);
            }
        }
    }

    private void apply(Map<Block, BlockData> blocks)
    {
        blocks.forEach((block, data) ->
        {
            // A chest, a sign, or another block entity keeps data that restoring the type loses.
            if (!block.getChunk().isLoaded() || block.getState() instanceof TileState)
            {
                return;
            }
            originals.computeIfAbsent(block, Block::getBlockData);
            block.setBlockData(data, false);
        });
    }

    private void rewrite(Set<Block> owned, Map<Block, BlockData> blocks)
    {
        blocks.forEach((block, data) ->
        {
            if (!owned.contains(block) || !originals.containsKey(block) || !block.getChunk().isLoaded())
            {
                return;
            }
            block.setBlockData(data, false);
        });
    }

    // Every block of a placement lies within one chunk of its anchor, so the one-chunk radius
    // covers a footprint that spills into a neighbour. Run directly when this thread already owns
    // that region; otherwise cross once. The region task owns the anchor chunk, and Folia keeps
    // an empty buffer between regions, so every loaded neighbour of the anchor belongs to it too.
    private CompletableFuture<Void> runOwning(Location anchor, Runnable runnable, boolean owned)
    {
        if (Bukkit.isOwnedByCurrentRegion(anchor, 1))
        {
            runnable.run();
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> completion = new CompletableFuture<>();
        ScheduledTask task = Bukkit.getRegionScheduler().run(module.plugin(), anchor, ignored ->
        {
            try
            {
                runnable.run();
            }
            finally
            {
                completion.complete(null);
            }
        });
        if (owned)
        {
            module.ownTask(task);
        }
        return completion;
    }

    public final class Placement
    {
        private final Location anchor;
        private final Set<Block> blocks;
        private final AtomicBoolean active = new AtomicBoolean(true);
        private volatile ScheduledTask fadeTask;

        private Placement(Location anchor, Set<Block> blocks)
        {
            this.anchor = anchor.clone();
            this.blocks = blocks;
        }

        public void repaint(Map<Block, BlockData> blocks)
        {
            if (!active.get())
            {
                return;
            }
            Map<Block, BlockData> snapshot = Map.copyOf(blocks);
            runOwning(anchor, () -> rewrite(this.blocks, snapshot), true);
        }

        public void revert()
        {
            revertInternal(true);
        }

        public boolean active()
        {
            return active.get();
        }

        // The owned revert runs once. The unowned shutdown revert always runs, because an already
        // inactive placement may still have its owned restore queued on a task Plex is about to
        // cancel. The placement stays tracked until its restore actually ran.
        private CompletableFuture<Void> revertInternal(boolean owned)
        {
            if (owned && !active.compareAndSet(true, false))
            {
                return CompletableFuture.completedFuture(null);
            }
            active.set(false);

            ScheduledTask fade = fadeTask;
            if (fade != null)
            {
                fade.cancel();
                fadeTask = null;
            }
            return runOwning(anchor, () ->
            {
                restore(blocks);
                placements.remove(this);
            }, owned);
        }
    }
}
