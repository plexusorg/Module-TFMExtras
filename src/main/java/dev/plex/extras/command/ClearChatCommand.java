package dev.plex.extras.command;

import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.rank.enums.Rank;
import dev.plex.util.PlexUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandParameters(name = "clearchat", description = "Clears the chat", aliases = "cc,cleanchat,chatclear")
@CommandPermissions(level = Rank.ADMIN, permission = "plex.tfmextras.clearchat")
public class ClearChatCommand extends PlexCommand
{
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        Bukkit.getOnlinePlayers().stream().filter(p -> !silentCheckRank(p, Rank.ADMIN, "plex.tfmextras.clearchat"))
                .forEach(p ->
                {
                    for (int i = 0; i < 100; i++)
                    {
                        send(p, "");
                    }
                });
        PlexUtils.broadcast(messageComponent("chatCleared", sender.getName()));
        return null;
    }
}
