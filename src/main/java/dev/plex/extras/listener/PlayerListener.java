package dev.plex.extras.listener;

import dev.plex.Plex;
import dev.plex.extras.TFMExtras;
import dev.plex.listener.PlexListener;
import dev.plex.util.PlexUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener extends PlexListener
{
    @EventHandler
    public void onAuto(PlayerJoinEvent event)
    {
        if (TFMExtras.getModule().getConfig().getStringList("server.clear-on-join").contains(event.getPlayer().getName()))
        {
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    event.getPlayer().getInventory().clear();
                }
            }.runTaskLater(Plex.get(), 1);
        }
        if (TFMExtras.getModule().getConfig().getStringList("server.teleport-on-join").contains(event.getPlayer().getName()))
        {
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    event.getPlayer().teleportAsync(TFMExtras.getRandomLocation(event.getPlayer().getWorld()));
                }
            }.runTaskLater(Plex.get(), 1);
        }
    }

    @EventHandler
    public void createPlayerWorld(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        final Pair<World, Boolean> world = TFMExtras.getModule().getSlimeWorldHook().createPlayerWorld(player.getUniqueId());
        if (world.getRight())
        {
            player.sendMessage(PlexUtils.messageComponent("createdPlayerWorld"));
        }
    }
}
