package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, this::seatSelf));
        command.then(word("player").suggests((context, builder) -> suggestMatching(builder, onlinePlayerNames()))
                .executes(context -> executeCommand(context,
                        (sender, player) -> seatOther(sender, string(context, "player"))))
                .then(greedyString("ignored").executes(context -> executeCommand(context,
                        (sender, player) -> seatOther(sender, string(context, "player"))))));
    }

    private Component seatSelf(CommandSender sender, Player player)
    {
        if (!(sender instanceof Player))
        {
            return usage();
        }
        seat(sender, player, false);
        return null;
    }

    private Component seatOther(CommandSender sender, String playerName)
    {
        Player target = getNonNullPlayer(playerName);
        scheduler().runEntity(target, () -> seat(sender, target, true));
        return null;
    }

    private void seat(CommandSender sender, Player target, boolean other)
    {
        if (target.isInsideVehicle())
        {
            target.leaveVehicle();
        }
        List<Entity> minecarts = target.getNearbyEntities(100, 100, 100).stream()
                .filter(entity -> entity.getType() == EntityType.MINECART)
                .collect(Collectors.toList());
        if (minecarts.isEmpty())
        {
            send(sender, other
                    ? messageComponent("targetMinecartNotFound", target.getName())
                    : messageComponent("minecartNotFound"));
            return;
        }
        Entity minecart = findNearestEntity(target, minecarts);
        scheduler().runEntity(minecart, () -> minecart.addPassenger(target));
    }

    private Entity findNearestEntity(Player player, List<Entity> entities)
    {
        return entities.stream()
                .min(Comparator.comparingDouble(entity -> player.getLocation().distanceSquared(entity.getLocation())))
                .orElseThrow();
    }

}
