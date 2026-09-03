package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import dev.plex.command.source.RequiredCommandSource;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ExpelCommand extends SimplePlexCommand
{
    public ExpelCommand()
    {
        super(command("expel")
                .description("Pushes away nearby players")
                .usage("/expel <radius> <strength>")
                .aliases("push")
                .permission("plex.tfmextras.expel")
                .source(RequiredCommandSource.IN_GAME)
                .build());
    }
    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> executeTyped(player, null, null)));
        command.then(word("radius").executes(context -> executeCommand(context,
                        (sender, player) -> executeTyped(player, string(context, "radius"), null)))
                .then(word("strength").executes(context -> executeCommand(context,
                                (sender, player) -> executeTyped(player, string(context, "radius"), string(context, "strength"))))
                        .then(greedyString("ignored").executes(context -> executeCommand(context,
                                (sender, player) -> executeTyped(player, string(context, "radius"), string(context, "strength")))))));
    }

    private Component executeTyped(Player player, @Nullable String radiusValue, @Nullable String strengthValue)
    {
        double radius = 20.0;
        double strength = 5.0;

        if (radiusValue != null)
        {
            try
            {
                radius = Math.min(Double.parseDouble(radiusValue), 20.0);
            }
            catch (NumberFormatException ignored)
            {
                return usage();
            }
        }

        if (strengthValue != null)
        {
            try
            {
                strength = Math.min(Double.parseDouble(strengthValue), 10.0);
            }
            catch (NumberFormatException ignored)
            {
                return usage();
            }
        }

        Vector senderPos = player.getLocation().toVector();
        for (Player target : player.getWorld().getPlayers())
        {
            if (!target.equals(player))
            {
                expel(target, senderPos, radius, strength);
            }
        }
        return null;
    }

    private void expel(Player target, Vector senderPos, double radius, double strength)
    {
        scheduler().runEntity(target, () ->
        {
            Location targetPos = target.getLocation();
            Vector targetPosVec = targetPos.toVector();
            if (targetPosVec.distanceSquared(senderPos) < radius * radius)
            {
                target.setFlying(false);
                target.getWorld().createExplosion(targetPos, 0.0f, false);
                target.setVelocity(targetPosVec.subtract(senderPos).normalize().multiply(strength));
            }
        });
    }

}
