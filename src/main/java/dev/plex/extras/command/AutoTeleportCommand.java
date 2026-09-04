package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.api.player.PlexPlayerView;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.TFMExtras;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.plex.api.message.MessagePlaceholder.placeholder;

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
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, this::teleportSelf));
        command.then(word("player").suggests((context, builder) -> suggestMatching(builder, onlinePlayerNames()))
                .executes(context -> executeCommand(context,
                        (sender, player) -> togglePlayer(sender, string(context, "player"))))
                .then(greedyString("ignored").executes(context -> executeCommand(context,
                        (sender, player) -> togglePlayer(sender, string(context, "player"))))));
    }

    private Component teleportSelf(CommandSender sender, Player player)
    {
        if (sender instanceof ConsoleCommandSender) return usage();
        module.teleportRandom(player);
        return null;
    }

    private Component togglePlayer(CommandSender sender, String playerName)
    {
        checkPermission(sender, "plex.tfmextras.autotp.other");
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
        module.toggleConfigEntry("server.teleport-on-join", target.name()).whenComplete((enabled, failure) ->
        {
            if (failure != null) module.getLogger().error("Failed to update automatic teleporting", failure);
            else send(sender, messageComponent("modifiedAutoTeleport", placeholder("player", target.name()), placeholder("state", enabled ? "now" : "no longer")));
        });
    }

}
