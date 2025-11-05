package dev.plex.extras.listener;

import dev.plex.extras.TFMExtras;
import dev.plex.extras.island.PlayerWorld;
import dev.plex.extras.island.info.IslandPermissions;
import dev.plex.listener.PlexListener;
import dev.plex.util.PlexUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class WorldListener extends PlexListener
{
    @EventHandler
    public void onBuild(BlockPlaceEvent event)
    {
        if (!TFMExtras.getModule().enableIslands()) return;
        if (!TFMExtras.getModule().getSlimeWorldHook().isWorldLoaded(event.getPlayer().getWorld().getName())) return;
        final UUID worldOwner = UUID.fromString(event.getPlayer().getWorld().getName());
        final PlayerWorld world = TFMExtras.getModule().getIslandHandler().loadedIslands().get(worldOwner);
        if (world.owner().equals(event.getPlayer().getUniqueId())) return;
        if (world.editPermission() == IslandPermissions.NOBODY)
        {
            event.getPlayer().sendMessage(PlexUtils.messageComponent("cantModifyIsland"));
            event.setCancelled(true);
            event.setBuild(false);
            return;
        }

        if (world.editPermission() == IslandPermissions.MEMBERS && !world.members().contains(event.getPlayer().getUniqueId()))
        {
            event.getPlayer().sendMessage(PlexUtils.messageComponent("cantModifyIsland"));
            event.setCancelled(true);
            event.setBuild(false);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event)
    {
        if (!TFMExtras.getModule().enableIslands()) return;
        if (!TFMExtras.getModule().getSlimeWorldHook().isWorldLoaded(event.getPlayer().getWorld().getName())) return;
        final UUID worldOwner = UUID.fromString(event.getPlayer().getWorld().getName());
        final PlayerWorld world = TFMExtras.getModule().getIslandHandler().loadedIslands().get(worldOwner);
        if (world.owner().equals(event.getPlayer().getUniqueId())) return;
        if (world.editPermission() == IslandPermissions.NOBODY)
        {
            event.getPlayer().sendMessage(PlexUtils.messageComponent("cantModifyIsland"));
            event.setCancelled(true);
            return;
        }

        if (world.editPermission() == IslandPermissions.MEMBERS && !world.members().contains(event.getPlayer().getUniqueId()))
        {
            event.getPlayer().sendMessage(PlexUtils.messageComponent("cantModifyIsland"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (!TFMExtras.getModule().enableIslands()) return;
        if (!TFMExtras.getModule().getSlimeWorldHook().isWorldLoaded(event.getPlayer().getWorld().getName())) return;
        final UUID worldOwner = UUID.fromString(event.getPlayer().getWorld().getName());
        final PlayerWorld world = TFMExtras.getModule().getIslandHandler().loadedIslands().get(worldOwner);
        if (world.owner().equals(event.getPlayer().getUniqueId())) return;
        if (world.interactPermission() == IslandPermissions.NOBODY)
        {
            event.getPlayer().sendMessage(PlexUtils.messageComponent("cantModifyIsland"));
            event.setCancelled(true);
            return;
        }

        if (world.interactPermission() == IslandPermissions.MEMBERS && !world.members().contains(event.getPlayer().getUniqueId()))
        {
            event.getPlayer().sendMessage(PlexUtils.messageComponent("cantModifyIsland"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event)
    {
        if (!TFMExtras.getModule().enableIslands()) return;
        if (!TFMExtras.getModule().getSlimeWorldHook().isWorldLoaded(event.getPlayer().getWorld().getName())) return;
        final UUID worldOwner = UUID.fromString(event.getPlayer().getWorld().getName());
        final PlayerWorld world = TFMExtras.getModule().getIslandHandler().loadedIslands().get(worldOwner);
        if (world.owner().equals(event.getPlayer().getUniqueId())) return;
        if (world.visitPermission() == IslandPermissions.NOBODY)
        {
            event.getPlayer().sendMessage(PlexUtils.messageComponent("cantVisitIsland"));
            event.getPlayer().teleportAsync(event.getFrom().getSpawnLocation());
            return;
        }

        if (world.visitPermission() == IslandPermissions.MEMBERS && !world.members().contains(event.getPlayer().getUniqueId()))
        {
            event.getPlayer().sendMessage(PlexUtils.messageComponent("cantVisitIsland"));
            event.getPlayer().teleportAsync(event.getFrom().getSpawnLocation());
        }
    }
}
