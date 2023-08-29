package dev.plex.extras.listener;

import dev.plex.extras.TFMExtras;
import dev.plex.extras.jumppads.JumpPads;
import dev.plex.extras.jumppads.Mode;
import dev.plex.listener.PlexListener;
import dev.plex.util.PlexLog;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

public class JumpPadsListener extends PlexListener
{
    JumpPads jumpPads = TFMExtras.getModule().jumpPads;

    @EventHandler(priority = EventPriority.NORMAL)
    public void jumppadsAction(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        Vector playerVector = player.getVelocity().clone();
        Block block = event.getTo().getBlock();
        Map<Block, JumpPads.Wrap> blockWrapMap = jumpPads.blockWrapMap(block);
        if (!jumpPads.playerModeMap.containsKey(player.getUniqueId()))
        {
            return;
        }

        Mode mode = jumpPads.playerModeMap.get(player.getUniqueId());
        if (mode == Mode.NORMAL)
        {
            Block below = block.getRelative(BlockFace.DOWN);
            if (jumpPads.wool.getValues().contains(below.getType()))
            {
                if (event.getFrom().getY() > block.getY() + 0.1 && ((int)event.getTo().getY() == block.getY()))
                {
                    Vector vector = playerVector.multiply(new Vector(0.0, jumpPads.SCALAR * jumpPads.STRENGTH, 0.0));
                    if (vector.getY() < 0)
                    {
                        vector = vector.multiply(new Vector(0, -1, 0));
                    }
                    PlexLog.debug("New Velocity: {0}", vector.toString());
                    player.setFallDistance(0);
                    player.setVelocity(vector);
                }

            }
        }

        if (mode.equals(Mode.ENHANCED))
        {
            blockWrapMap.forEach((b, w) ->
            {
                if (jumpPads.wool.getValues().contains(b.getType()))
                {
                    if (!(event.getFrom().getY() > block.getY() + 0.1 && ((int)event.getTo().getY() == block.getY())))
                    {
                        return;
                    }
                    if (w.y() == -1)
                    {
                        playerVector.add(new Vector(0.0, jumpPads.SCALAR * jumpPads.STRENGTH, 0.0));
                    }

                    switch (w.x())
                    {
                        case (-1):
                            playerVector.add(new Vector(-jumpPads.SCALAR * jumpPads.STRENGTH, 0.0, 0.0));
                        case (1):
                            playerVector.add(new Vector(jumpPads.SCALAR * jumpPads.STRENGTH, 0.0, 0.0));
                    }

                    switch (w.z())
                    {
                        case (-1):
                            playerVector.add(new Vector(0.0, 0.0, -jumpPads.SCALAR * jumpPads.STRENGTH));
                        case (1):
                            playerVector.add(new Vector(0.0, 0.0, jumpPads.SCALAR * jumpPads.STRENGTH));
                    }

                    player.setVelocity(playerVector);
                }
            });
        }

        if (mode == Mode.EXTREME)
        {
            Block below = block.getRelative(BlockFace.DOWN);
            if (jumpPads.wool.getValues().contains(below.getType()))
            {
                if (event.getFrom().getY() > block.getY() + 0.1 && ((int)event.getTo().getY() == block.getY()))
                {
                    player.setVelocity(jumpPads.extreme(playerVector));
                }
            }

        }

        if (mode.equals(Mode.ULTIMATE))
        {
            blockWrapMap.forEach((b, w) ->
            {
                if (jumpPads.wool.getValues().contains(b.getType()))
                {
                    if (w.y() == -1)
                    {
                        jumpPads.extreme(playerVector.add(new Vector(0.0, jumpPads.SCALAR * jumpPads.STRENGTH, 0.0)));
                    }

                    switch (w.x())
                    {
                        case (-1):
                            jumpPads.extreme(playerVector.add(new Vector(-jumpPads.SCALAR * jumpPads.STRENGTH, 0.0, 0.0)));
                        case (1):
                            jumpPads.extreme(playerVector.add(new Vector(jumpPads.SCALAR * jumpPads.STRENGTH, 0.0, 0.0)));
                    }

                    switch (w.z())
                    {
                        case (-1):
                            jumpPads.extreme(playerVector.add(new Vector(0.0, 0.0, -jumpPads.SCALAR * jumpPads.STRENGTH)));
                        case (1):
                            jumpPads.extreme(playerVector.add(new Vector(0.0, 0.0, jumpPads.SCALAR * jumpPads.STRENGTH)));

                    }

                    player.setVelocity(playerVector);
                }
            });

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void cleanup(PlayerQuitEvent event)
    {
        jumpPads.playerModeMap.remove(event.getPlayer().getUniqueId());
    }
}
