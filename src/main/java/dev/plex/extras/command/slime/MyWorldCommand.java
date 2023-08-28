package dev.plex.extras.command.slime;

import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.command.exception.PlayerNotFoundException;
import dev.plex.command.source.RequiredCommandSource;
import dev.plex.extras.TFMExtras;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Taah
 * @since 7:43 PM [24-08-2023]
 */

@CommandParameters(name = "myworld", usage = "/<command> <create | goto | manage | members | shared | add | remove | settings> [player]")
@CommandPermissions( permission = "plex.tfmextras.myworld", source = RequiredCommandSource.IN_GAME)
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
                player.teleportAsync(world.getSpawnLocation());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                return null;
            }
        }
        return null;
    }
}
