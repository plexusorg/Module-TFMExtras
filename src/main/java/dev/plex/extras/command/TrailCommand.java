package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import dev.plex.command.source.RequiredCommandSource;
import dev.plex.extras.fun.Trails;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TrailCommand extends SimplePlexCommand
{
    private final Trails trails;

    public TrailCommand(Trails trails)
    {
        super(command("trail")
                .description("Toggles a rainbow trail that fades behind you")
                .permission("plex.tfmextras.trail")
                .source(RequiredCommandSource.IN_GAME)
                .build());
        this.trails = trails;
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, this::executeTyped));
        command.then(greedyString("ignored").executes(context -> executeCommand(context, this::executeTyped)));
    }

    private Component executeTyped(CommandSender sender, Player player)
    {
        return trails.toggle(player) ? messageComponent("trailEnabled") : messageComponent("trailDisabled");
    }

}
