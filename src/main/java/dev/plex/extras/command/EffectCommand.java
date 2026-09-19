package dev.plex.extras.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.plex.api.message.ActionBroadcast;
import dev.plex.command.SimplePlexCommand;
import dev.plex.command.exception.CommandFailException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.registry.RegistryKey;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public class EffectCommand extends SimplePlexCommand
{
    private static final String GIVE = "plex.tfmextras.effect.give";
    private static final String CLEAR = "plex.tfmextras.effect.clear";
    private static final String INFINITE = "infinite";
    private static final int DEFAULT_TICKS = 600;
    private static final int MAX_SECONDS = 1_000_000;

    public EffectCommand()
    {
        super(command("effect")
                .description("Gives or clears potion effects for yourself, another player, or everyone")
                .usage("/effect give <player|-a> <effect> [seconds|infinite] [amplifier] [hideParticles] | /effect clear [player|-a] [effect]")
                .build());
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.requires(source -> source.getSender().hasPermission(GIVE) || source.getSender().hasPermission(CLEAR));
        command.executes(context -> executeCommand(context, (sender, player) -> usage()));
        command.then(Commands.literal("give").requires(source -> source.getSender().hasPermission(GIVE))
                .then(targetArgument("player", GIVE + ".others")
                        .then(Commands.argument("effect", ArgumentTypes.resource(RegistryKey.MOB_EFFECT))
                                .executes(context -> give(context, null, 0, false))
                                .then(word("duration").suggests((context, builder) -> suggestMatching(builder, List.of(INFINITE)))
                                        .executes(context -> give(context, string(context, "duration"), 0, false))
                                        .then(Commands.argument("amplifier", IntegerArgumentType.integer(0, 255))
                                                .executes(context -> give(context, string(context, "duration"),
                                                        IntegerArgumentType.getInteger(context, "amplifier"), false))
                                                .then(Commands.argument("hideParticles", BoolArgumentType.bool())
                                                        .executes(context -> give(context, string(context, "duration"),
                                                                IntegerArgumentType.getInteger(context, "amplifier"),
                                                                BoolArgumentType.getBool(context, "hideParticles")))))))));
        command.then(Commands.literal("clear").requires(source -> source.getSender().hasPermission(CLEAR))
                .executes(context -> clear(context, null, null))
                .then(targetArgument("player", CLEAR + ".others")
                        .executes(context -> clear(context, string(context, "player"), null))
                        .then(Commands.argument("effect", ArgumentTypes.resource(RegistryKey.MOB_EFFECT))
                                .executes(context -> clear(context, string(context, "player"),
                                        context.getArgument("effect", PotionEffectType.class))))));
    }

    private int give(CommandContext<CommandSourceStack> context, @Nullable String duration, int amplifier, boolean hideParticles)
    {
        return executeCommand(context, (sender, player) ->
        {
            String name = string(context, "player");
            List<Player> targets = resolveTargets(sender, name, GIVE + ".others");
            PotionEffectType type = context.getArgument("effect", PotionEffectType.class);
            PotionEffect effect = new PotionEffect(type, durationTicks(duration, type), amplifier, false, !hideParticles);

            if (!ALL_TARGETS.equals(name))
            {
                Player target = targets.get(0);
                Component success = messageComponent("effectGiven", Placeholder.unparsed("effect", type.key().asString()),
                        Placeholder.unparsed("amplifier", String.valueOf(amplifier)), Placeholder.unparsed("player", target.getName()));
                return applyToTarget(sender, target, () -> target.addPotionEffect(effect), success);
            }

            Component everyone = messageComponent("effectGivenEveryone", Placeholder.unparsed("sender", sender.getName()),
                    Placeholder.unparsed("effect", type.key().asString()), Placeholder.unparsed("amplifier", String.valueOf(amplifier)));
            return applyToEveryone(sender, targets, target -> () -> target.addPotionEffect(effect), everyone);
        });
    }

    private int clear(CommandContext<CommandSourceStack> context, @Nullable String name, @Nullable PotionEffectType type)
    {
        return executeCommand(context, (sender, player) ->
        {
            List<Player> targets = resolveTargets(sender, name, CLEAR + ".others");
            Function<Player, BooleanSupplier> mutation = target -> () ->
            {
                if (type == null) return target.clearActivePotionEffects();
                if (!target.hasPotionEffect(type)) return false;
                target.removePotionEffect(type);
                return !target.hasPotionEffect(type);
            };

            if (!ALL_TARGETS.equals(name))
            {
                Player target = targets.get(0);
                Component success = messageComponent(type == null ? "effectsCleared" : "effectCleared",
                        Placeholder.unparsed("player", target.getName()),
                        Placeholder.unparsed("effect", type == null ? "" : type.key().asString()));
                return applyToTarget(sender, target, mutation.apply(target), success);
            }

            Component everyone = messageComponent(type == null ? "effectsClearedEveryone" : "effectClearedEveryone",
                    Placeholder.unparsed("sender", sender.getName()),
                    Placeholder.unparsed("effect", type == null ? "" : type.key().asString()));
            return applyToEveryone(sender, targets, mutation, everyone);
        });
    }

    private int durationTicks(@Nullable String duration, PotionEffectType type)
    {
        if (duration == null) return type.isInstant() ? 1 : DEFAULT_TICKS;
        if (duration.equals(INFINITE)) return PotionEffect.INFINITE_DURATION;

        try
        {
            int seconds = Integer.parseInt(duration);
            if (seconds >= 1 && seconds <= MAX_SECONDS) return type.isInstant() ? seconds : seconds * 20;
        }
        catch (NumberFormatException ignored)
        {
            // A non-numeric duration is a usage mistake, so both branches report the usage text below.
        }
        throw new CommandFailException(MiniMessage.miniMessage().serialize(usage()));
    }

    // The mutation is the only work owned by the target: a self target runs it here, and another player's
    // potion state crosses to that player's entity scheduler. The sender hears the outcome after it runs.
    private @Nullable Component applyToTarget(CommandSender sender, Player target, BooleanSupplier mutation, Component success)
    {
        Component unchanged = messageComponent("effectUnchanged", Placeholder.unparsed("player", target.getName()));
        if (target.equals(sender))
        {
            return mutation.getAsBoolean() ? success : unchanged;
        }

        runOnOwner(sender, target, mutation, changed -> sender.sendMessage(changed ? success : unchanged));
        return null;
    }

    // Applies the mutation to every resolved target and announces once after the first change; an
    // unchanged target is skipped silently instead of reporting per player.
    private @Nullable Component applyToEveryone(CommandSender sender, List<Player> targets,
                                                Function<Player, BooleanSupplier> mutation, Component everyone)
    {
        ActionBroadcast announcement = api().messages().captureActionBroadcast(sender);
        AtomicBoolean announced = new AtomicBoolean();
        Runnable done = () ->
        {
            if (announced.compareAndSet(false, true)) announcement.send(everyone);
        };
        for (Player target : targets)
        {
            runOnOwner(sender, target, mutation.apply(target), changed ->
            {
                if (changed) done.run();
            });
        }
        return null;
    }

    private void runOnOwner(CommandSender sender, Player target, BooleanSupplier mutation, Consumer<Boolean> resultHandler)
    {
        if (target.equals(sender))
        {
            resultHandler.accept(mutation.getAsBoolean());
            return;
        }

        Component unavailable = messageComponent("effectPlayerUnavailable", Placeholder.unparsed("player", target.getName()));
        if (ownTask(target.getScheduler().run(taskOwner(), task -> resultHandler.accept(mutation.getAsBoolean()),
                () -> sender.sendMessage(unavailable))) == null)
        {
            sender.sendMessage(unavailable);
        }
    }
}
