package dev.plex.extras.listener;

import dev.plex.extras.TFMExtras;
import dev.plex.listener.PlexListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener extends PlexListener
{
    @EventHandler
    public void onAuto(PlayerJoinEvent event)
    {
        if (TFMExtras.getModule().getConfig().getStringList("server.clear-on-join").contains(event.getPlayer().getName()))
        {
            TFMExtras.plexApi().scheduler().runEntityLater(event.getPlayer(), () -> event.getPlayer().getInventory().clear(), 1);
        }
        if (TFMExtras.getModule().getConfig().getStringList("server.teleport-on-join").contains(event.getPlayer().getName()))
        {
            TFMExtras.plexApi().scheduler().runEntityLater(event.getPlayer(), () -> event.getPlayer().teleportAsync(TFMExtras.getRandomLocation(event.getPlayer().getWorld())), 1);
        }
    }

}
