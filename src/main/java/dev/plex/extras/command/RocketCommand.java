package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.api.message.ActionBroadcast;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.fun.Rockets;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class RocketCommand extends SimplePlexCommand
{
    private final Rockets rockets;

    public RocketCommand(Rockets rockets)
    {
        super(command("rocket")
                .description("Launches a player into the sky")
                .usage("/<command> [player]")
                .permission("plex.tfmextras.rocket")
                .build());
        this.rockets = rockets;
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context,
                (sender, player) -> player == null ? usage() : launch(sender, player.getName())));
        command.then(word("player").suggests((context, builder) ->
        {
            CommandSender sender = context.getSource().getSender();
            return suggestMatching(builder, sender.hasPermission("plex.tfmextras.rocket.others")
                    ? onlinePlayerNames() : sender instanceof Player player ? List.of(player.getName()) : List.of());
        })
                .executes(context -> executeCommand(context,
                        (sender, player) -> launch(sender, string(context, "player")))));
    }

    private @Nullable Component launch(CommandSender sender, String name)
    {
        Player target = getNonNullPlayer(name);
        boolean self = target.equals(sender);
        if (!self)
        {
            checkPermission(sender, "plex.tfmextras.rocket.others");
        }
        Component launched = messageComponent("rocketLaunched",
                Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("player", target.getName()));
        Runnable done;
        if (self)
        {
            done = () -> sender.sendMessage(launched);
        }
        else
        {
            ActionBroadcast announcement = api().messages().captureActionBroadcast(sender);
            done = () -> announcement.send(launched);
        }
        Runnable retired = () -> sender.sendMessage(messageComponent("funPlayerUnavailable",
                Placeholder.unparsed("player", target.getName())));
        if (!rockets.launch(target, done, retired))
        {
            return messageComponent("rocketAlreadyFlying", Placeholder.unparsed("player", target.getName()));
        }
        return null;
    }
}
