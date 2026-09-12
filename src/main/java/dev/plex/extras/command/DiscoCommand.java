package dev.plex.extras.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.api.message.ActionBroadcast;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.fun.Disco;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscoCommand extends SimplePlexCommand
{
    private static final int DEFAULT_SECONDS = 10;

    private final Disco disco;

    public DiscoCommand(Disco disco)
    {
        super(command("disco")
                .description("Turns the floor under a player into a dance floor")
                .usage("/<command> <player> [seconds | stop]")
                .permission("plex.tfmextras.disco")
                .build());
        this.disco = disco;
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> usage()));
        command.then(word("player").suggests((context, builder) -> suggestMatching(builder, onlinePlayerNames()))
                .executes(context -> executeCommand(context,
                        (sender, player) -> start(sender, string(context, "player"), DEFAULT_SECONDS)))
                .then(Commands.literal("stop").executes(context -> executeCommand(context,
                        (sender, player) -> stop(sender, string(context, "player")))))
                .then(Commands.argument("seconds", IntegerArgumentType.integer(1))
                        .executes(context -> executeCommand(context, (sender, player) -> start(sender,
                                string(context, "player"), IntegerArgumentType.getInteger(context, "seconds"))))));
    }

    private Component start(CommandSender sender, String name, int seconds)
    {
        Player target = getNonNullPlayer(name);
        int max = disco.maxSeconds();
        if (seconds > max)
        {
            return messageComponent("discoTooLong", Placeholder.unparsed("max", String.valueOf(max)));
        }

        ActionBroadcast announcement = api().messages().captureActionBroadcast(sender);
        Component started = messageComponent("discoStarted", Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("player", target.getName()), Placeholder.unparsed("seconds", String.valueOf(seconds)));
        Component unavailable = messageComponent("funPlayerUnavailable", Placeholder.unparsed("player", target.getName()));
        disco.start(target, seconds, () -> announcement.send(started), () -> sender.sendMessage(unavailable));
        return null;
    }

    private Component stop(CommandSender sender, String name)
    {
        Player target = getNonNullPlayer(name);
        ActionBroadcast announcement = api().messages().captureActionBroadcast(sender);
        if (!disco.stop(target.getUniqueId()))
        {
            return messageComponent("discoNotRunning", Placeholder.unparsed("player", target.getName()));
        }

        announcement.send(messageComponent("discoStopped", Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("player", target.getName())));
        return null;
    }
}
