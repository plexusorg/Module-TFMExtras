package dev.plex.command;

import dev.plex.Plex;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.rank.enums.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@CommandParameters(name = "cartsit", description = "Sit in nearest minecart. If target is in a minecart already, they will be ejected", aliases = "minecartsit")
@CommandPermissions(level = Rank.NONOP, permission = "plex.tfmextras.cartsit")
public class CartSitCommand extends PlexCommand
{
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        if (!(sender instanceof Player) && args.length == 0)
        {
            return usage("/cartsit <player>");
        }

        if (args.length == 0)
        {
            if (player.isInsideVehicle())
            {
                player.eject();
            }
            List<Entity> minecart = player.getNearbyEntities(100, 100, 100).stream().filter(entity -> entity.getType() == EntityType.MINECART).collect(Collectors.toList());
            if (minecart.isEmpty())
            {
                return MiniMessage.miniMessage().deserialize("<red>Could not find a nearby minecart!");
            }
            findNearestEntity(player, minecart).whenComplete((entity, throwable) ->
            {
                Bukkit.getScheduler().runTask(Plex.get(), () -> entity.addPassenger(player));
            });
            return null;
        }
        Player target = getNonNullPlayer(args[0]);
        if (target.isInsideVehicle())
        {
            target.eject();
        }
        List<Entity> minecart = target.getNearbyEntities(100, 100, 100).stream().filter(entity -> entity.getType() == EntityType.MINECART).collect(Collectors.toList());
        if (minecart.isEmpty())
        {
            return MiniMessage.miniMessage().deserialize("<red>Could not find a nearby minecart near " + target.getName() + "!");
        }
        findNearestEntity(target, minecart).whenComplete((entity, throwable) ->
        {
            Bukkit.getScheduler().runTask(Plex.get(), () -> entity.addPassenger(target));
        });

        return null;
    }

    public CompletableFuture<Entity> findNearestEntity(Player player, List<Entity> entities)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            Entity nearest = entities.get(0);
            for (int i = 0; i < entities.size(); i++)
            {
                Entity e = entities.get(i);
                if (player.getLocation().distance(e.getLocation()) < player.getLocation().distance(nearest.getLocation()))
                {
                    nearest = e;
                }
            }
            return nearest;
        });
    }
}
