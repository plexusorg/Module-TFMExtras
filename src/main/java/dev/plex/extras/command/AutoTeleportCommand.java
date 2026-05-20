package dev.plex.extras.command;

import com.google.common.collect.ImmutableList;
import dev.plex.api.player.PlexPlayerView;
import dev.plex.command.SimplePlexCommand;
import dev.plex.command.exception.PlayerNotFoundException;
import dev.plex.extras.TFMExtras;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AutoTeleportCommand extends SimplePlexCommand
{
    private final TFMExtras module;

    public AutoTeleportCommand(TFMExtras module)
    {
        super(command("autoteleport")
                .description("If a player is specified, it will toggle whether or not the player is automatically teleported when they join. If no player is specified, you will be randomly teleported")
                .usage("/<command> [player]")
                .aliases("autotp,rtp,randomtp,tpr")
                .permission("plex.tfmextras.autotp")
                .build());
        this.module = module;
    }

    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        if (args.length == 0)
        {
            if (sender instanceof ConsoleCommandSender)
            {
                return usage();
            }
            api().scheduler().runEntity(player, () -> player.teleportAsync(module.getRandomLocation(player.getWorld())));
            return null;
        }
        checkPermission(sender, "plex.tfmextras.autotp.other");
        PlexPlayerView target = api().players().byName(args[0]).orElseThrow(PlayerNotFoundException::new);
        List<String> names = module.getConfig().getStringList("server.teleport-on-join");
        boolean isEnabled = names.contains(target.name());
        if (!isEnabled)
        {
            names.add(target.name());
        }
        else
        {
            names.remove(target.name());
        }
        module.getConfig().set("server.teleport-on-join", names);
        module.getConfig().save();
        isEnabled = !isEnabled;
        return messageComponent("modifiedAutoTeleport", target.name(), isEnabled ? "now" : "no longer");
    }

    @Override
    protected @NotNull List<String> suggestions(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        return args.length == 1 && silentCheckPermission(sender, this.getPermission()) ? onlinePlayerNames() : ImmutableList.of();
    }
}
