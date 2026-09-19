package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.api.message.ActionBroadcast;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.fun.Rockets;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class RocketCommand extends SimplePlexCommand
{
    private static final String OTHERS_PERMISSION = "plex.tfmextras.rocket.others";
    private final Rockets rockets;

    public RocketCommand(Rockets rockets)
    {
        super(command("rocket")
                .description("Launches yourself, another player, or everyone into the sky")
                .usage("/<command> [player | -a]")
                .permission("plex.tfmextras.rocket")
                .build());
        this.rockets = rockets;
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> execute(sender, null)));
        command.then(targetArgument("player", OTHERS_PERMISSION)
                .executes(context -> executeCommand(context,
                        (sender, player) -> execute(sender, string(context, "player")))));
    }

    private @Nullable Component execute(CommandSender sender, @Nullable String name)
    {
        List<Player> targets = resolveTargets(sender, name, OTHERS_PERMISSION);
        if (!ALL_TARGETS.equals(name))
        {
            return launch(sender, targets.get(0));
        }

        ActionBroadcast announcement = api().messages().captureActionBroadcast(sender);
        AtomicBoolean announced = new AtomicBoolean();
        Component everyone = messageComponent("rocketLaunchedEveryone", Placeholder.unparsed("sender", sender.getName()));
        Runnable done = () ->
        {
            if (announced.compareAndSet(false, true)) announcement.send(everyone);
        };
        for (Player target : targets)
        {
            Runnable retired = () -> sender.sendMessage(messageComponent("funPlayerUnavailable",
                    Placeholder.unparsed("player", target.getName())));
            rockets.launch(target, done, retired);
        }
        return null;
    }

    private @Nullable Component launch(CommandSender sender, Player target)
    {
        boolean self = target.equals(sender);
        Component launched = messageComponent(self ? "rocketLaunchedSelf" : "rocketLaunched",
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
