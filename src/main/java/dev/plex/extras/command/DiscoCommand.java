package dev.plex.extras.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.api.message.ActionBroadcast;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.fun.Disco;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class DiscoCommand extends SimplePlexCommand
{
    private static final String OTHERS_PERMISSION = "plex.tfmextras.disco.others";
    private static final int DEFAULT_SECONDS = 10;
    private final Disco disco;

    public DiscoCommand(Disco disco)
    {
        super(command("disco")
                .description("Starts a dance floor for yourself, another player, or everyone")
                .usage("/<command> [seconds | stop] [player | -a]")
                .permission("plex.tfmextras.disco")
                .build());
        this.disco = disco;
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context,
                (sender, player) -> execute(sender, null, DEFAULT_SECONDS)));
        command.then(Commands.literal("stop")
                .executes(context -> executeCommand(context, (sender, player) -> execute(sender, null, null)))
                .then(targetArgument("player", OTHERS_PERMISSION)
                        .executes(context -> executeCommand(context,
                                (sender, player) -> execute(sender, string(context, "player"), null)))));
        command.then(Commands.argument("seconds", IntegerArgumentType.integer(1))
                .executes(context -> executeCommand(context, (sender, player) -> execute(sender, null,
                        IntegerArgumentType.getInteger(context, "seconds"))))
                .then(targetArgument("player", OTHERS_PERMISSION)
                        .executes(context -> executeCommand(context, (sender, player) -> execute(sender,
                                string(context, "player"), IntegerArgumentType.getInteger(context, "seconds"))))));
        command.then(targetArgument("player", OTHERS_PERMISSION)
                .executes(context -> executeCommand(context,
                        (sender, player) -> execute(sender, string(context, "player"), DEFAULT_SECONDS))));
    }

    private Component execute(CommandSender sender, @Nullable String name, @Nullable Integer seconds)
    {
        if (seconds != null && seconds > disco.maxSeconds())
        {
            return messageComponent("discoTooLong", Placeholder.unparsed("max", String.valueOf(disco.maxSeconds())));
        }

        List<Player> targets = resolveTargets(sender, name, OTHERS_PERMISSION);
        if (!ALL_TARGETS.equals(name))
        {
            apply(sender, targets.get(0), seconds);
            return null;
        }

        ActionBroadcast announcement = api().messages().captureActionBroadcast(sender);
        AtomicBoolean announced = new AtomicBoolean();
        Component message = messageComponent(seconds == null ? "discoEveryoneStopped" : "discoEveryoneStarted",
                Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("seconds", String.valueOf(seconds)));
        Runnable done = () ->
        {
            if (announced.compareAndSet(false, true)) announcement.send(message);
        };
        for (Player target : targets)
        {
            if (seconds == null)
            {
                if (disco.stop(target.getUniqueId())) done.run();
            }
            else
            {
                disco.start(target, seconds, done, () -> sender.sendMessage(messageComponent("funPlayerUnavailable",
                        Placeholder.unparsed("player", target.getName()))));
            }
        }
        return null;
    }

    private void apply(CommandSender sender, Player target, @Nullable Integer seconds)
    {
        String suffix = target.equals(sender) ? "Self" : "Other";
        if (seconds == null)
        {
            sender.sendMessage(messageComponent((disco.stop(target.getUniqueId()) ? "discoStopped" : "discoNotRunning") + suffix,
                    Placeholder.unparsed("player", target.getName())));
            return;
        }
        Component started = messageComponent("discoStarted" + suffix, Placeholder.unparsed("seconds", String.valueOf(seconds)),
                Placeholder.unparsed("player", target.getName()));
        Component unavailable = messageComponent("funPlayerUnavailable", Placeholder.unparsed("player", target.getName()));
        disco.start(target, seconds, () -> sender.sendMessage(started), () -> sender.sendMessage(unavailable));
    }
}
