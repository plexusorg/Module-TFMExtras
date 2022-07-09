package dev.plex.listener;

import dev.plex.Plex;
import dev.plex.TFMExtras;
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
}
