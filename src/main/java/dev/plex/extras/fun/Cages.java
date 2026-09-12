package dev.plex.extras.fun;

import dev.plex.extras.TFMExtras;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BoundingBox;

public final class Cages implements Listener
{
    private final TFMExtras module;
    private final TemporaryBlocks blocks;
    private final Map<UUID, Cage> cages = new ConcurrentHashMap<>();

    public Cages(TFMExtras module, TemporaryBlocks blocks)
    {
        this.module = module;
        this.blocks = blocks;
    }

    public void cage(Player target, BlockData outer, BlockData inner, Runnable done, Runnable retired)
    {
        EntityOwner.run(module, target, () -> build(target, outer, inner, done), retired);
    }

    public boolean uncage(UUID playerId)
    {
        Cage cage = cages.remove(playerId);
        if (cage == null) return false;
        cage.placement().revert();
        return true;
    }

    public void uncageAll()
    {
        cages.keySet().forEach(this::uncage);
    }

    @EventHandler(ignoreCancelled = true)
    public void keepCaged(PlayerMoveEvent event)
    {
        Cage cage = cages.get(event.getPlayer().getUniqueId());
        if (cage == null) return;
        if (outside(cage, event.getTo())) event.setCancelled(true);
    }

    // PlayerTeleportEvent has its own handler list in Paper, so the move handler above never
    // receives a teleport.
    @EventHandler(ignoreCancelled = true)
    public void keepCaged(PlayerTeleportEvent event)
    {
        Cage cage = cages.get(event.getPlayer().getUniqueId());
        if (cage == null) return;
        if (outside(cage, event.getTo())) event.setCancelled(true);
    }

    @EventHandler
    public void cleanup(PlayerQuitEvent event)
    {
        uncage(event.getPlayer().getUniqueId());
    }

    private boolean outside(Cage cage, Location to)
    {
        return to.getWorld() != cage.world() || !cage.interior().contains(to.toVector());
    }

    // Runs on the target's region, so its location and the surrounding blocks are owned here.
    private void build(Player target, BlockData outer, BlockData inner, Runnable done)
    {
        UUID playerId = target.getUniqueId();
        Cage existing = cages.remove(playerId);
        if (existing != null) existing.placement().revert();

        Location center = target.getLocation();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        TemporaryBlocks.Placement placement = blocks.place(center,
                shell(center.getWorld(), cx, cy, cz, outer, inner), 0);
        cages.put(playerId, new Cage(placement, center.getWorld(),
                new BoundingBox(cx - 1, cy, cz - 1, cx + 2, cy + 3, cz + 2)));
        done.run();
    }

    private Map<Block, BlockData> shell(World world, int cx, int cy, int cz, BlockData outer, BlockData inner)
    {
        Map<Block, BlockData> shell = new HashMap<>();
        for (int x = cx - 2; x <= cx + 2; x++)
        {
            for (int y = cy - 1; y <= cy + 3; y++)
            {
                for (int z = cz - 2; z <= cz + 2; z++)
                {
                    boolean wall = x == cx - 2 || x == cx + 2 || y == cy - 1 || y == cy + 3
                            || z == cz - 2 || z == cz + 2;
                    shell.put(world.getBlockAt(x, y, z), wall ? outer : inner);
                }
            }
        }
        return shell;
    }

    private record Cage(TemporaryBlocks.Placement placement, World world, BoundingBox interior)
    {
    }
}
