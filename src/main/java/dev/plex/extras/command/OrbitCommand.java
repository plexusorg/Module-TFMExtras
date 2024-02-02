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

import java.util.List;

@CommandParameters(name = "orbit", description = "Accelerates the player at a fast rate", usage = "/<command> <target> [<<power> | stop>]")
@CommandPermissions(permission = "plex.tfmextras.orbit")
public class OrbitCommand extends PlexCommand
{
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
        return args.length == 1 && silentCheckPermission(sender, this.getPermission()) ? PlexUtils.getPlayerNameList() : ImmutableList.of();
    }

    private void startOrbiting(Player player, int strength) {
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, Integer.MAX_VALUE, strength, false, false));
    }

    private void stopOrbiting(Player player) {
        player.removePotionEffect(PotionEffectType.LEVITATION);
    }
}
