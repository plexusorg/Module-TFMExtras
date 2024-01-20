package dev.plex.extras.command;

import com.google.common.collect.ImmutableList;
import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.util.PlexUtils;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Credit to AcidicCyanide <3
 * Credit to "TheDeus-Group" for the messages :)
 */

@CommandParameters(name = "emf", description = "Speak english.", usage = "/<command> <player>")
@CommandPermissions(permission = "plex.tfmextras.emf")
public class EnglishMfCommand extends PlexCommand
{
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        if (args.length == 0)
        {
            return usage();
        }
        Player target = getNonNullPlayer(args[0]);
        target.sendMessage(mmString("<red>ENGLISH MOTHERFUCKER, Do you speak it!?"));
        PlexUtils.broadcast("<red>" + sender.getName() + " is sick of " + target.getName() + " not speaking English!");
        target.setHealth(0);
        target.getWorld().strikeLightningEffect(target.getLocation());
        return null;
    }

    @Override
    public @NotNull List<String> smartTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        return args.length == 1 && silentCheckPermission(sender, this.getPermission()) ? PlexUtils.getPlayerNameList() : ImmutableList.of();
    }
}
