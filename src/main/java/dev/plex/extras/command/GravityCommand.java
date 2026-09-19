package dev.plex.extras.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.extras.fun.SessionAttributes;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.attribute.Attribute;
import org.jetbrains.annotations.Nullable;

public class GravityCommand extends SessionAttributeCommand
{
    private static final double LOW_GRAVITY = 0.02;
    private static final double HIGH_GRAVITY = 0.2;

    public GravityCommand(SessionAttributes attributes)
    {
        super(command("gravity")
                .description("Sets the gravity of yourself, another player, or everyone for this session")
                .usage("/<command> <low | normal | high | reset | value> [player | -a]")
                .permission("plex.tfmextras.gravity")
                .build(), attributes, Attribute.GRAVITY, "gravitySet", "gravityReset", "gravitySetEveryone", "gravityResetEveryone");
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> usage()));
        command.then(valueLiteral("low", LOW_GRAVITY));
        command.then(valueLiteral("normal", null));
        command.then(valueLiteral("high", HIGH_GRAVITY));
        command.then(valueLiteral("reset", null));
        command.then(Commands.argument("value", DoubleArgumentType.doubleArg(0.01, 1.0))
                .executes(context -> executeCommand(context, (sender, player) -> apply(sender, null,
                        DoubleArgumentType.getDouble(context, "value"))))
                .then(targetArgument("player", getPermission() + ".others")
                        .executes(context -> executeCommand(context, (sender, player) -> apply(sender,
                                string(context, "player"), DoubleArgumentType.getDouble(context, "value"))))));
    }

    // A fixed named value (low/normal/high/reset) needs the same optional target branch as the others.
    private LiteralArgumentBuilder<CommandSourceStack> valueLiteral(String name, @Nullable Double value)
    {
        return Commands.literal(name)
                .executes(context -> executeCommand(context, (sender, player) -> apply(sender, null, value)))
                .then(targetArgument("player", getPermission() + ".others")
                        .executes(context -> executeCommand(context,
                                (sender, player) -> apply(sender, string(context, "player"), value))));
    }
}
