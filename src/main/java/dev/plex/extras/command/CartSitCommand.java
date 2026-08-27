package dev.plex.extras.command;

import com.google.common.collect.ImmutableList;
import dev.plex.command.SimplePlexCommand;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CartSitCommand extends SimplePlexCommand
{
    public CartSitCommand()
    {
        super(command("cartsit")
                .description("Sit in nearest minecart. If target is in a minecart already, they will be ejected")
                .usage("/<command> <player>")
                .aliases("minecartsit")
                .permission("plex.tfmextras.cartsit")
                .build());
    }
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        if (!(sender instanceof Player) && args.length == 0)
        {
            return usage();
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
                return messageComponent("minecartNotFound");
            }
            Entity entity = findNearestEntity(player, minecart);
            scheduler().runEntity(entity, () -> entity.addPassenger(player));
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
            return messageComponent("targetMinecartNotFound", target.getName());
        }
        Entity entity = findNearestEntity(target, minecart);
        scheduler().runEntity(entity, () -> entity.addPassenger(target));

        return null;
    }

    public Entity findNearestEntity(Player player, List<Entity> entities)
    {
        return entities.stream()
                .min(Comparator.comparingDouble(entity -> player.getLocation().distanceSquared(entity.getLocation())))
                .orElseThrow();
    }

    @Override
    protected @NotNull List<String> suggestions(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        return args.length == 1 && silentCheckPermission(sender, this.getPermission()) ? onlinePlayerNames() : ImmutableList.of();
    }
}
