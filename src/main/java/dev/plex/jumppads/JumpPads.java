package dev.plex.jumppads;

import dev.plex.TFMExtras;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class JumpPads
{
    public final Map<UUID, Mode> playerModeMap = new HashMap<>();
    public final double SCALAR = 0.8;
    public final double STRENGTH = TFMExtras.getModule().getConfig().getInt("server.jumppad_strength", 1) + 0.1F;
    public final double EXTREME = STRENGTH + 0.5;
    public final Tag<Material> wool = Tag.WOOL;

    public Vector extreme(Vector vector)
    {
        return vector.multiply(STRENGTH * SCALAR * ThreadLocalRandom.current().nextInt(3, 6));
    }

    public void addPlayer(Player player, Mode mode)
    {
        playerModeMap.put(player.getUniqueId(), mode);
    }

    public void updatePlayer(Player player, Mode mode)
    {
        playerModeMap.replace(player.getUniqueId(), mode);
    }

    public void removePlayer(Player player)
    {
        playerModeMap.remove(player.getUniqueId());
    }

    public Mode get(Player player)
    {
        return playerModeMap.get(player.getUniqueId());
    }

    public final Map<Block, Wrap> blockWrapMap(Block block)
    {
        return new HashMap<>()
        {{
            put(block.getRelative(BlockFace.DOWN), new Wrap(0, -1, 0));
            put(block.getRelative(BlockFace.EAST), new Wrap(1, 0, 0));
            put(block.getRelative(BlockFace.WEST), new Wrap(-1, 0, 0));
            put(block.getRelative(BlockFace.NORTH), new Wrap(0, 0, 1));
            put(block.getRelative(BlockFace.SOUTH), new Wrap(0, 0, -1));
        }};
    }

    public record Wrap(int x, int y, int z)
    {
    }
}