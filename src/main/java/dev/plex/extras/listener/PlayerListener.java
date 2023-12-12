package dev.plex.extras.listener;

import dev.plex.Plex;
import dev.plex.extras.TFMExtras;
import dev.plex.listener.PlexListener;
import dev.plex.util.PlexLog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
    public void unloadWorld(PlayerQuitEvent event)
    {
        final Player player = event.getPlayer();
        PlexLog.debug("Slime World Loaded: {0}", TFMExtras.getModule().getSlimeWorldHook().isWorldLoaded(player.getUniqueId().toString()));
        PlexLog.debug("World Loaded: {0}", Bukkit.getWorld(player.getUniqueId().toString()) != null);
        if (TFMExtras.getModule().getSlimeWorldHook().isWorldLoaded(player.getUniqueId().toString()) && Bukkit.getWorld(player.getUniqueId().toString()) != null)
        {
            Bukkit.unloadWorld(player.getUniqueId().toString(), true);
        }
    }
}
