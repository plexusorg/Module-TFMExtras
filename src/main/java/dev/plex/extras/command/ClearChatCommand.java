package dev.plex.extras.command;

import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.util.PlexUtils;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandParameters(name = "clearchat", description = "Clears the chat", aliases = "cc,cleanchat,chatclear")
@CommandPermissions(permission = "plex.tfmextras.clearchat")
public class ClearChatCommand extends PlexCommand
{
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        Bukkit.getOnlinePlayers().stream().filter(p -> !silentCheckPermission(p, "plex.tfmextras.clearchat"))
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

    @Override
    public @NotNull List<String> smartTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        return Collections.emptyList();
    }
}
