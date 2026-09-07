package dev.plex.extras.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.plex.command.SimplePlexCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.registry.RegistryKey;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.ToIntFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public class EffectCommand extends SimplePlexCommand
{
    private static final String GIVE = "plex.tfmextras.effect.give";
    private static final String CLEAR = "plex.tfmextras.effect.clear";

    public EffectCommand()
    {
        super(command("effect")
                .description("Gives or clears potion effects for yourself or another player")
                .usage("/effect give <player> <effect> [seconds|infinite] [amplifier] [hideParticles] | /effect clear [player] [effect]")
                .build());
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.requires(source -> source.getSender().hasPermission(GIVE) || source.getSender().hasPermission(CLEAR));
        command.executes(context -> executeCommand(context, (sender, player) -> usage()));
        command.then(Commands.literal("give").requires(source -> source.getSender().hasPermission(GIVE))
                .then(word("player").suggests((context, builder) -> suggestMatching(builder, targets(context, GIVE)))
                        .then(Commands.argument("effect", ArgumentTypes.resource(RegistryKey.MOB_EFFECT))
                                .executes(context -> give(context, null, 0, false))
                                .then(giveDuration(Commands.argument("seconds", IntegerArgumentType.integer(1, 1_000_000)),
                                        context -> IntegerArgumentType.getInteger(context, "seconds")))
                                .then(giveDuration(Commands.literal("infinite"), context -> PotionEffect.INFINITE_DURATION)))));
        command.then(Commands.literal("clear").requires(source -> source.getSender().hasPermission(CLEAR))
                .executes(context -> executeCommand(context, (sender, player) -> clear(sender, player, null, null)))
                .then(word("player").suggests((context, builder) -> suggestMatching(builder, targets(context, CLEAR)))
                        .executes(context -> executeCommand(context, (sender, player) -> clear(sender, player, string(context, "player"), null)))
                        .then(Commands.argument("effect", ArgumentTypes.resource(RegistryKey.MOB_EFFECT))
                                .executes(context -> executeCommand(context, (sender, player) -> clear(sender, player,
                                        string(context, "player"), context.getArgument("effect", PotionEffectType.class)))))));
    }

    private ArgumentBuilder<CommandSourceStack, ?> giveDuration(ArgumentBuilder<CommandSourceStack, ?> duration,
                                                               ToIntFunction<CommandContext<CommandSourceStack>> seconds)
    {
        return duration.executes(context -> give(context, seconds.applyAsInt(context), 0, false))
                .then(Commands.argument("amplifier", IntegerArgumentType.integer(0, 255))
                        .executes(context -> give(context, seconds.applyAsInt(context), IntegerArgumentType.getInteger(context, "amplifier"), false))
                        .then(Commands.argument("hideParticles", BoolArgumentType.bool())
                                .executes(context -> give(context, seconds.applyAsInt(context), IntegerArgumentType.getInteger(context, "amplifier"),
                                        BoolArgumentType.getBool(context, "hideParticles")))));
    }

    private List<String> targets(CommandContext<CommandSourceStack> context, String permission)
    {
        CommandSender sender = context.getSource().getSender();
        if (sender.hasPermission(permission + ".others")) return onlinePlayerNames();
        return sender instanceof Player player ? List.of(player.getName()) : List.of();
    }

    private int give(CommandContext<CommandSourceStack> context, @Nullable Integer seconds, int amplifier, boolean hideParticles)
    {
        return executeCommand(context, (sender, player) ->
        {
            checkPermission(sender, GIVE);
            Player target = target(sender, player, string(context, "player"), GIVE);
            if (target == null) return messageComponent("playerNotFound");
            PotionEffectType type = context.getArgument("effect", PotionEffectType.class);
            int duration;
            if (seconds == null) duration = type.isInstant() ? 1 : 600;
            else if (seconds == PotionEffect.INFINITE_DURATION || type.isInstant()) duration = seconds;
            else duration = seconds * 20;
            PotionEffect effect = new PotionEffect(type, duration, amplifier, false, !hideParticles);
            Component success = messageComponent("effectGiven", Placeholder.unparsed("effect", type.key().asString()),
                    Placeholder.unparsed("amplifier", String.valueOf(amplifier)), Placeholder.unparsed("player", target.getName()));
            return updateEffects(sender, player, target, () -> target.addPotionEffect(effect), success);
        });
    }

    private Component clear(CommandSender sender, @Nullable Player player, @Nullable String name, @Nullable PotionEffectType type)
    {
        checkPermission(sender, CLEAR);
        if (name == null && player == null) return messageComponent("effectSpecifyPlayer");
        Player target = target(sender, player, name, CLEAR);
        if (target == null) return messageComponent("playerNotFound");
        Component success = messageComponent(type == null ? "effectsCleared" : "effectCleared",
                Placeholder.unparsed("player", target.getName()),
                Placeholder.unparsed("effect", type == null ? "" : type.key().asString()));
        return updateEffects(sender, player, target, () ->
        {
            if (type == null) return target.clearActivePotionEffects();
            if (!target.hasPotionEffect(type)) return false;
            target.removePotionEffect(type);
            return !target.hasPotionEffect(type);
        }, success);
    }

    private @Nullable Player target(CommandSender sender, @Nullable Player player, @Nullable String name, String permission)
    {
        Player target = name == null ? player : Bukkit.getPlayerExact(name);
        if (target != null && target != player) checkPermission(sender, permission + ".others");
        return target;
    }

    private @Nullable Component updateEffects(CommandSender sender, @Nullable Player player, Player target,
                                              BooleanSupplier mutation, Component success)
    {
        Component unchanged = messageComponent("effectUnchanged", Placeholder.unparsed("player", target.getName()));
        if (target == player) return mutation.getAsBoolean() ? success : unchanged;

        Component unavailable = messageComponent("effectPlayerUnavailable", Placeholder.unparsed("player", target.getName()));
        // Cross only for potion state; report the actual result after the mutation completes.
        if (ownTask(target.getScheduler().run(taskOwner(), task ->
                sender.sendMessage(mutation.getAsBoolean() ? success : unchanged), () -> sender.sendMessage(unavailable))) == null)
        {
            sender.sendMessage(unavailable);
        }
        return null;
    }
}
