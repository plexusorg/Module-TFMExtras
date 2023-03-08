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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@CommandParameters(name = "autoclear", description = "Toggle whether or not a player has their inventory automatically cleared when they join", usage = "/<command> <player>", aliases = "aclear,ac")
@CommandPermissions(level = Rank.ADMIN, permission = "plex.tfmextras.autoclear")
public class AutoClearCommand extends PlexCommand
{
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        if (args.length == 0)
        {
            return usage();
        }
        PlexPlayer target = DataUtils.getPlayer(args[0]);
        if (target == null)
        {
            throw new PlayerNotFoundException();
        }
        List<String> names = TFMExtras.getModule().getConfig().getStringList("server.clear-on-join");
        boolean isEnabled = names.contains(target.getName());
        if (!isEnabled)
        {
            names.add(target.getName());
        }
        else
        {
            names.remove(target.getName());
        }
        TFMExtras.getModule().getConfig().set("server.clear-on-join", names);
        TFMExtras.getModule().getConfig().save();
        isEnabled = !isEnabled;
        return messageComponent("modifiedAutoClear", target.getName(), isEnabled ? "now" : "no longer");
    }
}
