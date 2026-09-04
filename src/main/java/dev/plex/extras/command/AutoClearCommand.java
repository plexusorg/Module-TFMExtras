package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.api.player.PlexPlayerView;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.TFMExtras;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.plex.api.message.MessagePlaceholder.placeholder;

public class AutoClearCommand extends SimplePlexCommand
{
    private final TFMExtras module;

    public AutoClearCommand(TFMExtras module)
    {
        super(command("autoclear")
                .description("Toggle whether or not a player has their inventory automatically cleared when they join")
                .usage("/<command> <player>")
                .aliases("aclear,ac")
                .permission("plex.tfmextras.autoclear")
                .build());
        this.module = module;
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> usage()));
        command.then(playerArgument().executes(context -> executeCommand(context,
                        (sender, player) -> executeTyped(sender, string(context, "player"))))
                .then(greedyString("ignored").executes(context -> executeCommand(context,
                        (sender, player) -> executeTyped(sender, string(context, "player"))))));
    }

    private com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> playerArgument()
    {
        return word("player").suggests((context, builder) -> suggestMatching(builder, onlinePlayerNames()));
    }

    private Component executeTyped(CommandSender sender, String playerName)
    {
        api().players().byName(playerName).whenComplete((result, failure) ->
        {
            if (failure != null)
            {
                module.getLogger().error("Failed to look up player {}", playerName, failure);
                send(sender, Component.text("Player lookup failed."));
                return;
            }
            if (result.isEmpty())
            {
                send(sender, messageComponent("playerNotFound"));
                return;
            }
            toggle(sender, result.get());
        });
        return null;
    }

    private void toggle(CommandSender sender, PlexPlayerView target)
    {
        module.toggleConfigEntry("server.clear-on-join", target.name()).whenComplete((enabled, failure) ->
        {
            if (failure != null) module.getLogger().error("Failed to update automatic inventory clearing", failure);
            else send(sender, messageComponent("modifiedAutoClear", placeholder("player", target.name()), placeholder("state", enabled ? "now" : "no longer")));
        });
    }

}
