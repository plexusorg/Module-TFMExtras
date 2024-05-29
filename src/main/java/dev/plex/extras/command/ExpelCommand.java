package dev.plex.extras.command;

import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.util.PlexLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CommandParameters(name = "expel", description = "Pushes away nearby players", usage = "/expel <radius> <strength>", aliases = "push")
@CommandPermissions(permission = "plex.tfmextras.expel")
public class ExpelCommand extends PlexCommand
{
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

        List<String> pushedPlayers = new ArrayList<>();

        final Vector senderPos = player.getLocation().toVector();
        final List<Player> players = player.getWorld().getPlayers();

        for (final Player target : players)
        {
            if (target.equals(player))
            {
                continue;
            }

            final Location targetPos = target.getLocation();
            final Vector targetPosVec = targetPos.toVector();

            if (targetPosVec.distanceSquared(senderPos) < (radius * radius))
            {
                target.setFlying(false);

                target.getWorld().createExplosion(targetPos, 0.0f, false);
                target.setVelocity(targetPosVec.subtract(senderPos).normalize().multiply(strength));

                pushedPlayers.add(target.getName());
            }
        }

        if (!pushedPlayers.isEmpty())
        {
            return MiniMessage.miniMessage().deserialize("<gray>Pushed away players: <white><em>" + String.join("<reset><gray>, <white><em>", pushedPlayers));
        }

        return null;
    }

    @Override
    public @NotNull List<String> smartTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args)
    {
        if (args.length == 1)
        {
            return Collections.singletonList("<radius>");
        }
        else if (args.length == 2)
        {
            return Collections.singletonList("<strength>");
        }
        return Collections.emptyList();
    }
}
