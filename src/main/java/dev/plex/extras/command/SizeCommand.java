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
                .description("Sets the size of a player for this session")
                .usage("/<command> <player> <scale|reset>")
                .permission("plex.tfmextras.size")
                .build(), attributes, Attribute.SCALE, "sizeSet", "sizeReset");
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> usage()));
        command.then(word("player").suggests((context, builder) -> suggestMatching(builder, targets(context)))
                .then(Commands.literal("reset").executes(context -> executeCommand(context,
                        (sender, player) -> apply(sender, player, string(context, "player"), null))))
                .then(Commands.argument("scale", DoubleArgumentType.doubleArg(0.1, 10.0))
                        .executes(context -> executeCommand(context, (sender, player) -> apply(sender, player,
                                string(context, "player"), DoubleArgumentType.getDouble(context, "scale"))))));
    }
}
