package dev.plex.extras.command.slime;

import dev.plex.Plex;
import dev.plex.cache.DataUtils;
import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.command.exception.PlayerNotFoundException;
import dev.plex.command.source.RequiredCommandSource;
import dev.plex.extras.TFMExtras;
import dev.plex.extras.island.PlayerWorld;
import dev.plex.extras.island.info.IslandPermissions;
import dev.plex.player.PlexPlayer;
import dev.plex.util.PlexUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandParameters(name = "myworld", usage = "/<command> <create | goto | info | invite | remove | settings> [player]")
@CommandPermissions(permission = "plex.tfmextras.myworld", source = RequiredCommandSource.IN_GAME)
public class MyWorldCommand extends PlexCommand
{
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        assert player != null;
        if (args.length == 0)
        {
            return usage();
        }
        switch (args[0].toLowerCase())
        {
            case "create" ->
            {
                if (TFMExtras.getModule().getSlimeWorldHook().isWorldLoaded(player.getUniqueId().toString()))
                {
                    return messageComponent("playerWorldExists");
                }
                TFMExtras.getModule().getSlimeWorldHook().createPlayerWorld(player.getUniqueId());
                return messageComponent("createdPlayerWorld");
            }
            case "goto" ->
            {
                if (args.length == 1)
                {
                        if (!TFMExtras.getModule().getSlimeWorldHook().isWorldLoaded(player.getUniqueId().toString()))
                        {
                            return messageComponent("selfPlayerWorldNotFound");
                        }
                    World world = Bukkit.getWorld(player.getUniqueId().toString());
                    if (world == null)
                    {
                        return messageComponent("worldLoadError");
                    }
                    player.teleportAsync(world.getSpawnLocation());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    return null;
                }
                final Player target = Bukkit.getPlayer(args[1]);
                if (target == null)
                {
                    throw new PlayerNotFoundException();
                }
                if (!TFMExtras.getModule().getSlimeWorldHook().isWorldLoaded(target.getUniqueId().toString()))
                {
                    return messageComponent("playerWorldNotFound");
                }
                World world = Bukkit.getWorld(target.getUniqueId().toString());
                if (world == null)
                {
                    return messageComponent("worldLoadError");
                }
                final PlayerWorld playerWorld = TFMExtras.getModule().getIslandHandler().loadedIslands().get(target.getUniqueId());
                if (playerWorld != null)
                {
                    if (playerWorld.visitPermission() == IslandPermissions.NOBODY || (playerWorld.visitPermission() == IslandPermissions.MEMBERS && !playerWorld.members().contains(target.getUniqueId())))
                    {
                        return messageComponent("cannotAccessIsland");
                    }
                }
                player.teleportAsync(world.getSpawnLocation());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                return null;
            }
            case "settings" ->
            {
                if (!TFMExtras.getModule().getSlimeWorldHook().isWorldLoaded(player.getUniqueId().toString()))
                {
                    return messageComponent("selfPlayerWorldNotFound");
                }
                if (args.length != 3)
                {
                    return usage("/myworld settings <interact | edit | visit> <nobody | anyone | members>");
                }
                if (!args[1].equalsIgnoreCase("interact") && !args[1].equalsIgnoreCase("edit") && !args[1].equalsIgnoreCase("visit"))
                {
                    return usage("/myworld settings <interact | edit | visit> <nobody | anyone | members>");
                }
                final PlayerWorld playerWorld = TFMExtras.getModule().getIslandHandler().loadedIslands().get(player.getUniqueId());
                try {
                    final IslandPermissions permissions = IslandPermissions.valueOf(args[2].toUpperCase());
                    switch (args[1].toLowerCase())
                    {
                        case "interact" -> playerWorld.interactPermission(permissions);
                        case "edit" -> playerWorld.editPermission(permissions);
                        case "visit" -> playerWorld.visitPermission(permissions);
                    }
                    return messageComponent("islandPermissionUpdated", args[1].toUpperCase(), permissions.name());

                } catch (IllegalArgumentException e)
                {
                    return usage("/myworld settings <interact | edit | visit> <nobody | anyone | members>");
                }
            }

            case "invite" -> {
                final PlexPlayer plexPlayer = DataUtils.getPlayer(args[1], false);
                if (plexPlayer == null)
                {
                    throw new PlayerNotFoundException();
                }

                if (!TFMExtras.getModule().getSlimeWorldHook().isWorldLoaded(player.getUniqueId().toString()))
                {
                    return messageComponent("selfPlayerWorldNotFound");
                }

                final PlayerWorld playerWorld = TFMExtras.getModule().getIslandHandler().loadedIslands().get(player.getUniqueId());
                if (playerWorld.members().contains(plexPlayer.getPlayer().getUniqueId()))
                {
                    return messageComponent("islandMemberExists");
                }
                playerWorld.pendingInvites().add(plexPlayer.getUuid());
                if (Bukkit.getPlayer(plexPlayer.getUuid()) != null)
                {
                    final Player target = Bukkit.getPlayer(plexPlayer.getUuid());
                    assert target != null;
                    target.sendMessage(messageComponent("receivedInviteForIsland", player.getName()));
                }

                return messageComponent("sentInviteToIsland");
            }
        }
        return null;
    }

    @Override
    public @NotNull List<String> smartTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        if (silentCheckPermission(sender, this.getPermission()))
        {
            if (args.length == 1)
            {
                return Arrays.asList("create", "goto", "manage", "members", "shared", "add", "remove", "settings");
            }
            if (args.length == 2)
            {
                return PlexUtils.getPlayerNameList();
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
