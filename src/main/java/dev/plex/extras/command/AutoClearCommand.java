package dev.plex.extras.command;

import com.google.common.collect.ImmutableList;
import dev.plex.api.player.PlexPlayerView;
import dev.plex.command.SimplePlexCommand;
import dev.plex.command.exception.PlayerNotFoundException;
import dev.plex.extras.TFMExtras;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AutoClearCommand extends SimplePlexCommand
{
    public AutoClearCommand()
    {
        super(command("autoclear")
                .description("Toggle whether or not a player has their inventory automatically cleared when they join")
                .usage("/<command> <player>")
                .aliases("aclear,ac")
                .permission("plex.tfmextras.autoclear")
                .build());
    }
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        if (args.length == 0)
        {
            return usage();
        }
        PlexPlayerView target = TFMExtras.plexApi().players().byName(args[0]).orElseThrow(PlayerNotFoundException::new);
        List<String> names = TFMExtras.getModule().getConfig().getStringList("server.clear-on-join");
        boolean isEnabled = names.contains(target.name());
        if (!isEnabled)
        {
            names.add(target.name());
        }
        else
        {
            names.remove(target.name());
        }
        TFMExtras.getModule().getConfig().set("server.clear-on-join", names);
        TFMExtras.getModule().getConfig().save();
        isEnabled = !isEnabled;
        return messageComponent("modifiedAutoClear", target.name(), isEnabled ? "now" : "no longer");
    }


    @Override
    protected @NotNull List<String> suggestions(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        return args.length == 1 && silentCheckPermission(sender, this.getPermission()) ? onlinePlayerNames() : ImmutableList.of();
    }
}
