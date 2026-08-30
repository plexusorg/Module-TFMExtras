package dev.plex.extras.command;

import com.google.common.collect.ImmutableList;
import dev.plex.command.SimplePlexCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OrbitCommand extends SimplePlexCommand
{
    public OrbitCommand()
    {
        super(command("orbit")
                .description("Accelerates the player at a super fast rate")
                .usage("/<command> <target> [<<power> | stop>]")
                .permission("plex.tfmextras.orbit")
                .build());
    }
    private static final Map<UUID, Integer> isOrbited = new ConcurrentHashMap<>();

    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player playerSender, String[] args)
    {
        if (args.length == 0)
        {
            return usage();
        }

        Player targetPlayer = getNonNullPlayer(args[0]);

        int strength = 100;

        if (args.length >= 2)
        {
            if (args[1].equalsIgnoreCase("stop"))
            {
                stopOrbiting(targetPlayer);
                return messageComponent("stoppedOrbiting", targetPlayer.getName());
            }

            try
            {
                strength = Math.max(1, Math.min(150, Integer.parseInt(args[1])));
            }
            catch (NumberFormatException ex)
            {
                return null;
            }
        }

        if (orbitStrength(targetPlayer.getUniqueId()) != null)
        {
            return messageComponent("alreadyOrbited", targetPlayer.getName());
        }

        startOrbiting(targetPlayer, strength);
        broadcast(messageComponent("playerOrbited", sender.getName(), targetPlayer.getName()));
        return null;
    }

    @Override
    protected @NotNull List<String> suggestions(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        if (args.length == 1 && silentCheckPermission(sender, this.getPermission()))
        {
            return onlinePlayerNames();
        }
        else if (args.length == 2 && silentCheckPermission(sender, this.getPermission()))
        {
            return Collections.singletonList("stop");
        }
        return ImmutableList.of();
    }

    private void startOrbiting(Player player, int strength)
    {
        scheduler().runEntity(player, () ->
        {
            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, Integer.MAX_VALUE, strength, false, false));
            isOrbited.put(player.getUniqueId(), strength);
        });
    }

    private void stopOrbiting(Player player)
    {
        isOrbited.remove(player.getUniqueId());
        scheduler().runEntity(player, () -> player.removePotionEffect(PotionEffectType.LEVITATION));
    }

    public static Integer orbitStrength(UUID playerId)
    {
        return isOrbited.get(playerId);
    }
}
