package dev.plex.extras.command;

import dev.plex.command.SimplePlexCommand;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClearChatCommand extends SimplePlexCommand
{
    public ClearChatCommand()
    {
        super(command("clearchat")
                .description("Clears the chat")
                .aliases("cc,cleanchat,chatclear")
                .permission("plex.tfmextras.clearchat")
                .build());
    }
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        String senderName = sender.getName();
        Bukkit.getOnlinePlayers().forEach(p ->
        {
            if (!p.hasPermission("plex.tfmextras.clearchat"))
            {
                for (int i = 0; i < 100; i++)
                {
                    send(p, "");
                }
            }
        });
        broadcast(messageComponent("chatCleared", senderName));
        return null;
    }

    @Override
    protected @NotNull List<String> suggestions(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        return Collections.emptyList();
    }
}
