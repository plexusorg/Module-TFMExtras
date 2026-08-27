package dev.plex.extras.listener;

import dev.plex.extras.TFMExtras;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener
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
            module.scheduler().runEntityLater(event.getPlayer(), () -> event.getPlayer().getInventory().clear(), 1);
        }
        if (module.getConfig().getStringList("server.teleport-on-join").contains(event.getPlayer().getName()))
        {
            module.scheduler().runEntityLater(event.getPlayer(), () -> event.getPlayer().teleportAsync(module.getRandomLocation(event.getPlayer().getWorld())), 1);
        }
    }

}
