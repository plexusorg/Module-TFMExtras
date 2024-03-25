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

import net.kyori.adventure.text.Component;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@CommandParameters(name = "toggleclownfish", description = "Toggles the ability to use the clownfish for a specified player", usage = "/<command> <player>")
@CommandPermissions(permission = "plex.tfmextras.toggleclownfish")
public class RestrictClownfishCommand extends PlexCommand
{
    @Override
    protected Component execute(@NotNull CommandSender commandSender, @Nullable Player player, @NotNull String[] args)
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

        List<String> restrictedPlayers = TFMExtras.getModule().getConfig().getStringList("server.clownfish.restricted");

        boolean isRestricted = restrictedPlayers.contains(target.getUuid().toString());
        if (isRestricted)
        {
            restrictedPlayers.remove(target.getUuid().toString());
        }
        else
        {
            restrictedPlayers.add(target.getUuid().toString());
        }

        TFMExtras.getModule().getConfig().set("server.clownfish.restricted", restrictedPlayers);
        TFMExtras.getModule().getConfig().save();

        return messageComponent("restrictClownfish", target.getName(), isRestricted ? "now" : "no longer");
    }

    @Override
    public @NotNull List<String> smartTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return args.length == 1 && silentCheckPermission(sender, this.getPermission()) ? PlexUtils.getPlayerNameList() : ImmutableList.of();
    }
}
