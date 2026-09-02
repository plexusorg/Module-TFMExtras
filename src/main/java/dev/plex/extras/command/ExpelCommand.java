package dev.plex.extras.command;

import dev.plex.command.SimplePlexCommand;
import dev.plex.command.source.RequiredCommandSource;
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
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        double radius = 20.0;
        double strength = 5.0;

        if (args.length > 0)
        {
            try
            {
                radius = Math.min(Double.parseDouble(args[0]), 20.0);
            }
            catch (NumberFormatException ignored)
            {
                return usage();
            }
        }

        if (args.length > 1)
        {
            try
            {
                strength = Math.min(Double.parseDouble(args[1]), 10.0);
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

    @Override
    protected @NotNull List<String> suggestions(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args)
    {
        return Collections.emptyList();
    }
}
