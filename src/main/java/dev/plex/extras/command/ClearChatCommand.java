package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, this::executeTyped));
        command.then(greedyString("ignored").executes(context -> executeCommand(context, this::executeTyped)));
    }

    private Component executeTyped(CommandSender sender, Player player)
    {
        String senderName = sender.getName();
        scheduler().runGlobal(() ->
        {
            List.copyOf(Bukkit.getOnlinePlayers()).forEach(target -> scheduler().runEntity(target, () ->
            {
                if (!target.hasPermission("plex.tfmextras.clearchat"))
                {
                    for (int i = 0; i < 100; i++) send(target, "");
                }
            }));
            broadcast(messageComponent("chatCleared", senderName));
        });
        return null;
    }

}
