package dev.plex.extras.listener;

import dev.plex.extras.TFMExtras;
import dev.plex.listener.PlexListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener extends PlexListener
{
    private final TFMExtras module;

    public PlayerListener(TFMExtras module)
    {
        this.module = module;
    }

    @EventHandler
    public void onAuto(PlayerJoinEvent event)
    {
        if (module.getConfig().getStringList("server.clear-on-join").contains(event.getPlayer().getName()))
        {
            module.api().scheduler().runEntityLater(event.getPlayer(), () -> event.getPlayer().getInventory().clear(), 1);
        }
        if (module.getConfig().getStringList("server.teleport-on-join").contains(event.getPlayer().getName()))
        {
            module.api().scheduler().runEntityLater(event.getPlayer(), () -> event.getPlayer().teleportAsync(module.getRandomLocation(event.getPlayer().getWorld())), 1);
        }
    }

}
