package dev.plex.extras.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.extras.fun.SessionAttributes;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.attribute.Attribute;

public class GravityCommand extends SessionAttributeCommand
{
    private static final double LOW_GRAVITY = 0.02;
    private static final double HIGH_GRAVITY = 0.2;

    public GravityCommand(SessionAttributes attributes)
    {
        super(command("gravity")
                .description("Sets the gravity of a player for this session")
                .usage("/<command> <player> <low|normal|high|reset|value>")
                .permission("plex.tfmextras.gravity")
                .build(), attributes, Attribute.GRAVITY, "gravitySet", "gravityReset");
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> usage()));
        command.then(word("player").suggests((context, builder) -> suggestMatching(builder, targets(context)))
                .then(Commands.literal("low").executes(context -> executeCommand(context,
                        (sender, player) -> apply(sender, player, string(context, "player"), LOW_GRAVITY))))
                .then(Commands.literal("normal").executes(context -> executeCommand(context,
                        (sender, player) -> apply(sender, player, string(context, "player"), null))))
                .then(Commands.literal("high").executes(context -> executeCommand(context,
                        (sender, player) -> apply(sender, player, string(context, "player"), HIGH_GRAVITY))))
                .then(Commands.literal("reset").executes(context -> executeCommand(context,
                        (sender, player) -> apply(sender, player, string(context, "player"), null))))
                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.01, 1.0))
                        .executes(context -> executeCommand(context, (sender, player) -> apply(sender, player,
                                string(context, "player"), DoubleArgumentType.getDouble(context, "value"))))));
    }
}
