package dev.plex.command;

import dev.plex.TFMExtras;
import dev.plex.cache.DataUtils;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.command.exception.PlayerNotFoundException;
import dev.plex.player.PlexPlayer;
import dev.plex.rank.enums.Rank;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@CommandParameters(name = "autoteleport", description = "If a player is specified, it will toggle whether or not the player is automatically teleported when they join. If no player is specified, you will be randomly teleported", usage = "/<command> [player]", aliases = "autotp,rtp,randomtp,tpr")
@CommandPermissions(level = Rank.OP, permission = "plex.tfmextras.autotp")
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
        checkRank(sender, Rank.ADMIN, "plex.tfmextras.autotp.other");
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
}
