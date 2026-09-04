package dev.plex.extras.command;

import org.bukkit.Bukkit;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.plex.api.message.MessagePlaceholder.placeholder;

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
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, this::executeTyped));
        command.then(greedyString("ignored").executes(context -> executeCommand(context, this::executeTyped)));
    }

    private Component executeTyped(CommandSender sender, Player player)
    {
        String senderName = sender.getName();
        ownTask(Bukkit.getGlobalRegionScheduler().run(taskOwner(), task ->
        {
            List.copyOf(Bukkit.getOnlinePlayers()).forEach(target ->
            {
                if (!target.hasPermission("plex.tfmextras.clearchat"))
                {
                    for (int i = 0; i < 100; i++) send(target, "");
                }
            });
            broadcast(messageComponent("chatCleared", placeholder("sender", senderName)));
        }));
        return null;
    }

}
