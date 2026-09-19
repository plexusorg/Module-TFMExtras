package dev.plex.extras.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.api.message.ActionBroadcast;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.TFMExtras;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public class OrbitCommand extends SimplePlexCommand
{
    private static final String OTHERS_PERMISSION = "plex.tfmextras.orbit.others";
    private static final int DEFAULT_POWER = 100;
    private static final int MIN_POWER = 1;
    private static final int MAX_POWER = 150;

    private final TFMExtras module;

    public OrbitCommand(TFMExtras module)
    {
        super(command("orbit")
                .description("Accelerates yourself, another player, or everyone at a super fast rate")
                .usage("/<command> [power | stop] [player | -a]")
                .permission("plex.tfmextras.orbit")
                .build());
        this.module = module;
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> execute(sender, null, DEFAULT_POWER)));
        command.then(Commands.literal("stop")
                .executes(context -> executeCommand(context, (sender, player) -> execute(sender, null, null)))
                .then(targetArgument("player", OTHERS_PERMISSION)
                        .executes(context -> executeCommand(context,
                                (sender, player) -> execute(sender, string(context, "player"), null)))));
        command.then(Commands.argument("power", IntegerArgumentType.integer())
                .executes(context -> executeCommand(context, (sender, player) -> execute(sender, null,
                        clamp(IntegerArgumentType.getInteger(context, "power")))))
                .then(targetArgument("player", OTHERS_PERMISSION)
                        .executes(context -> executeCommand(context, (sender, player) -> execute(sender,
                                string(context, "player"), clamp(IntegerArgumentType.getInteger(context, "power")))))));
        command.then(targetArgument("player", OTHERS_PERMISSION)
                .executes(context -> executeCommand(context,
                        (sender, player) -> execute(sender, string(context, "player"), DEFAULT_POWER))));
    }

    private static int clamp(int power)
    {
        return Math.max(MIN_POWER, Math.min(MAX_POWER, power));
    }

    private Component execute(CommandSender sender, @Nullable String name, @Nullable Integer power)
    {
        List<Player> targets = resolveTargets(sender, name, OTHERS_PERMISSION);
        if (!ALL_TARGETS.equals(name))
        {
            Player target = targets.get(0);
            return power == null ? stop(target) : start(sender, target, power);
        }

        ActionBroadcast announcement = api().messages().captureActionBroadcast(sender);
        AtomicBoolean announced = new AtomicBoolean();
        Component everyone = messageComponent(power == null ? "orbitEveryoneStopped" : "orbitEveryoneStarted",
                Placeholder.unparsed("sender", sender.getName()));
        Runnable done = () ->
        {
            if (announced.compareAndSet(false, true)) announcement.send(everyone);
        };
        for (Player target : targets)
        {
            if (power == null)
            {
                stopOrbiting(target);
                done.run();
            }
            else if (module.orbitStrength(target.getUniqueId()) == null)
            {
                module.startOrbit(target, power);
                done.run();
            }
        }
        return null;
    }

    private Component stop(Player target)
    {
        stopOrbiting(target);
        return messageComponent("stoppedOrbiting", Placeholder.parsed("player", target.getName()));
    }

    private @Nullable Component start(CommandSender sender, Player target, int power)
    {
        if (module.orbitStrength(target.getUniqueId()) != null)
        {
            return messageComponent("alreadyOrbited", Placeholder.parsed("player", target.getName()));
        }

        module.startOrbit(target, power);
        if (target.equals(sender))
        {
            return messageComponent("orbitStartedSelf");
        }
        ActionBroadcast announcement = api().messages().captureActionBroadcast(sender);
        announcement.send(messageComponent("playerOrbited", Placeholder.parsed("sender", sender.getName()),
                Placeholder.parsed("player", target.getName())));
        return null;
    }

    private void stopOrbiting(Player player)
    {
        module.clearOrbitStrength(player.getUniqueId());
        ownTask(player.getScheduler().run(taskOwner(),
                ignored -> player.removePotionEffect(PotionEffectType.LEVITATION), null));
    }
}
