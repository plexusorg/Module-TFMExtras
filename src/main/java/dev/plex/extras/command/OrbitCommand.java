package dev.plex.extras.command;

import com.google.common.collect.ImmutableList;
import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.util.PlexUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@CommandParameters(name = "orbit", description = "Accelerates the player at a super fast rate", usage = "/<command> <target> [<<power> | stop>]")
@CommandPermissions(permission = "plex.tfmextras.orbit")
public class OrbitCommand extends PlexCommand
{
    private static final List<UUID> isOrbited = new ArrayList<>();

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

        startOrbiting(targetPlayer, strength);
        PlexUtils.broadcast(messageComponent("playerOrbited", sender.getName(), targetPlayer.getName()));
        return null;
    }

    @Override
    public @NotNull List<String> smartTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        if (args.length == 1 && silentCheckPermission(sender, this.getPermission()))
        {
            return PlexUtils.getPlayerNameList();
        }
        else if (args.length == 2 && silentCheckPermission(sender, this.getPermission()))
        {
            return Collections.singletonList("stop");
        }
        return ImmutableList.of();
    }

    private void startOrbiting(Player player, int strength)
    {
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, Integer.MAX_VALUE, strength, false, false));
        isOrbited.add(player.getUniqueId());
    }

    private void stopOrbiting(Player player)
    {
        player.removePotionEffect(PotionEffectType.LEVITATION);
        isOrbited.remove(player.getUniqueId());
    }

    public static boolean isPlayerOrbited(UUID playerId)
    {
        return isOrbited.contains(playerId);
    }
}
