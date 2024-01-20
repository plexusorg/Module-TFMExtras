package dev.plex.extras.command;

import com.google.common.collect.ImmutableList;
import dev.plex.cache.DataUtils;
import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.command.exception.PlayerNotFoundException;
import dev.plex.extras.TFMExtras;
import dev.plex.player.PlexPlayer;
import dev.plex.util.PlexUtils;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandParameters(name = "autoteleport", description = "If a player is specified, it will toggle whether or not the player is automatically teleported when they join. If no player is specified, you will be randomly teleported", usage = "/<command> [player]", aliases = "autotp,rtp,randomtp,tpr")
@CommandPermissions(permission = "plex.tfmextras.autotp")
public class AutoTeleportCommand extends PlexCommand
{
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        if (args.length == 0)
        {
            if (sender instanceof ConsoleCommandSender)
            {
                return usage();
            }
            player.teleportAsync(TFMExtras.getRandomLocation(player.getWorld()));
            return null;
        }
        checkPermission(sender, "plex.tfmextras.autotp.other");
        PlexPlayer target = DataUtils.getPlayer(args[0]);
        if (target == null)
        {
            throw new PlayerNotFoundException();
        }
        List<String> names = TFMExtras.getModule().getConfig().getStringList("server.teleport-on-join");
        boolean isEnabled = names.contains(target.getName());
        if (!isEnabled)
        {
            names.add(target.getName());
        }
        else
        {
            names.remove(target.getName());
        }
        TFMExtras.getModule().getConfig().set("server.teleport-on-join", names);
        TFMExtras.getModule().getConfig().save();
        isEnabled = !isEnabled;
        return messageComponent("modifiedAutoTeleport", target.getName(), isEnabled ? "now" : "no longer");
    }

    @Override
    public @NotNull List<String> smartTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        return args.length == 1 && silentCheckPermission(sender, this.getPermission()) ? PlexUtils.getPlayerNameList() : ImmutableList.of();
    }
}
