package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import dev.plex.command.source.RequiredCommandSource;
import dev.plex.extras.fun.Paintball;
import dev.plex.extras.fun.Palette;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class PaintballCommand extends SimplePlexCommand
{
    private static final List<String> COLORS = Arrays.stream(DyeColor.values())
            .map(color -> color.name().toLowerCase(Locale.ROOT))
            .toList();

    private final Paintball paintball;

    public PaintballCommand(Paintball paintball)
    {
        super(command("paintball")
                .description("Gives snowballs that paint whatever they hit for a few seconds")
                .usage("/<command> [color]")
                .permission("plex.tfmextras.paintball")
                .source(RequiredCommandSource.IN_GAME)
                .build());
        this.paintball = paintball;
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> give(player, null)));
        command.then(word("color").suggests((context, builder) -> suggestMatching(builder, COLORS))
                .executes(context -> executeCommand(context,
                        (sender, player) -> give(player, string(context, "color")))));
    }

    private Component give(Player player, @Nullable String color)
    {
        if (color != null && Palette.byName(color) == null)
        {
            return usage();
        }

        player.getInventory().addItem(paintball.item(color));
        return messageComponent("paintballGiven");
    }

}
