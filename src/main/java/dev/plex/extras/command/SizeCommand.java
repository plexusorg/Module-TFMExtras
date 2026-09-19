package dev.plex.extras.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.extras.fun.SessionAttributes;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.attribute.Attribute;

public class SizeCommand extends SessionAttributeCommand
{
    public SizeCommand(SessionAttributes attributes)
    {
        super(command("size")
                .description("Sets the size of yourself, another player, or everyone for this session")
                .usage("/<command> <scale | reset> [player | -a]")
                .permission("plex.tfmextras.size")
                .build(), attributes, Attribute.SCALE, "sizeSet", "sizeReset", "sizeSetEveryone", "sizeResetEveryone");
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> usage()));
        command.then(Commands.literal("reset")
                .executes(context -> executeCommand(context, (sender, player) -> apply(sender, null, null)))
                .then(targetArgument("player", getPermission() + ".others")
                        .executes(context -> executeCommand(context,
                                (sender, player) -> apply(sender, string(context, "player"), null)))));
        command.then(Commands.argument("scale", DoubleArgumentType.doubleArg(0.1, 10.0))
                .executes(context -> executeCommand(context, (sender, player) -> apply(sender, null,
                        DoubleArgumentType.getDouble(context, "scale"))))
                .then(targetArgument("player", getPermission() + ".others")
                        .executes(context -> executeCommand(context, (sender, player) -> apply(sender,
                                string(context, "player"), DoubleArgumentType.getDouble(context, "scale"))))));
    }
}
