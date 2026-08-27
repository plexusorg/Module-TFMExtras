package dev.plex.extras.command;

import com.google.common.collect.ImmutableList;
import dev.plex.command.SimplePlexCommand;
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

public class EnglishMfCommand extends SimplePlexCommand
{
    public EnglishMfCommand()
    {
        super(command("emf")
                .description("Speak english.")
                .usage("/<command> <player>")
                .permission("plex.tfmextras.emf")
                .build());
    }
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        if (args.length == 0)
        {
            return usage();
        }
        Player target = getNonNullPlayer(args[0]);
        scheduler().runEntity(target, () ->
        {
            target.sendMessage(mmString("<red>ENGLISH MOTHERFUCKER, Do you speak it!?"));
            target.setHealth(0);
            target.getWorld().strikeLightningEffect(target.getLocation());
        });
        broadcast("<red>" + sender.getName() + " is sick of " + target.getName() + " not speaking English!");
        return null;
    }

    @Override
    protected @NotNull List<String> suggestions(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        return args.length == 1 && silentCheckPermission(sender, this.getPermission()) ? onlinePlayerNames() : ImmutableList.of();
    }
}
