package dev.plex.extras.fun;

import dev.plex.extras.TFMExtras;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class Trails implements Listener
{
    private final TFMExtras module;
    private final TemporaryBlocks blocks;
    // Presence enables the trail; the value is the next rainbow colour for that player.
    private final Map<UUID, Integer> colorIndex = new ConcurrentHashMap<>();

    public Trails(TFMExtras module, TemporaryBlocks blocks)
    {
        this.module = module;
        this.blocks = blocks;
    }

    public boolean toggle(Player player)
    {
        UUID playerId = player.getUniqueId();
        if (colorIndex.remove(playerId) != null)
        {
            return false;
        }
        colorIndex.put(playerId, 0);
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        UUID playerId = event.getPlayer().getUniqueId();
        Integer index = colorIndex.get(playerId);
        if (index == null || !event.hasChangedBlock())
        {
            return;
        }

        Location to = event.getTo();
        Block below = to.getBlock().getRelative(BlockFace.DOWN);
        if (!below.getType().isSolid())
        {
            return;
        }

        // The move event runs on the moving player's region, which owns the block under the player.
        blocks.place(to, Map.of(below, Palette.at(index).createBlockData()),
                module.getConfig().getInt("server.fun.trail_fade_ticks", 100));
        colorIndex.put(playerId, index + 1);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        colorIndex.remove(event.getPlayer().getUniqueId());
    }
}
