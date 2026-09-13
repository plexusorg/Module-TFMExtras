package dev.plex.extras.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.fun.Disco;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class DiscoCommand extends SimplePlexCommand
{
    private static final int DEFAULT_SECONDS = 10;
    private final Disco disco;

    public DiscoCommand(Disco disco)
    {
        super(command("disco")
                .description("Starts a dance floor for yourself or everyone")
                .usage("/<command> [everyone] [seconds | stop]")
                .permission("plex.tfmextras.disco")
                .build());
        this.disco = disco;
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context,
                (sender, player) -> execute(sender, player, false, DEFAULT_SECONDS)));
        command.then(Commands.literal("stop").executes(context -> executeCommand(context,
                (sender, player) -> execute(sender, player, false, null))));
        command.then(Commands.argument("seconds", IntegerArgumentType.integer(1))
                .executes(context -> executeCommand(context, (sender, player) -> execute(sender, player, false,
                        IntegerArgumentType.getInteger(context, "seconds")))));
        command.then(Commands.literal("everyone")
                .requires(source -> source.getSender().hasPermission("plex.tfmextras.disco.everyone"))
                .executes(context -> executeCommand(context,
                        (sender, player) -> execute(sender, player, true, DEFAULT_SECONDS)))
                .then(Commands.literal("stop").executes(context -> executeCommand(context,
                        (sender, player) -> execute(sender, player, true, null))))
                .then(Commands.argument("seconds", IntegerArgumentType.integer(1))
                        .executes(context -> executeCommand(context, (sender, player) -> execute(sender, player, true,
                                IntegerArgumentType.getInteger(context, "seconds"))))));
    }

    private Component execute(CommandSender sender, @Nullable Player player, boolean everyone, @Nullable Integer seconds)
    {
        if (seconds != null && seconds > disco.maxSeconds())
        {
            return messageComponent("discoTooLong", Placeholder.unparsed("max", String.valueOf(disco.maxSeconds())));
        }
        if (!everyone)
        {
            if (player == null) return usage();
            apply(sender, player, seconds);
            return null;
        }

        for (String name : onlinePlayerNames())
        {
            Player target = Bukkit.getPlayerExact(name);
            if (target != null) apply(sender, target, seconds);
        }
        return null;
    }

    private void apply(CommandSender sender, Player target, @Nullable Integer seconds)
    {
        if (seconds == null)
        {
            sender.sendMessage(messageComponent(disco.stop(target.getUniqueId()) ? "discoStopped" : "discoNotRunning",
                    Placeholder.unparsed("sender", sender.getName()), Placeholder.unparsed("player", target.getName())));
            return;
        }
        Component started = messageComponent("discoStarted", Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("player", target.getName()), Placeholder.unparsed("seconds", String.valueOf(seconds)));
        Component unavailable = messageComponent("funPlayerUnavailable", Placeholder.unparsed("player", target.getName()));
        disco.start(target, seconds, () -> sender.sendMessage(started), () -> sender.sendMessage(unavailable));
    }
}
