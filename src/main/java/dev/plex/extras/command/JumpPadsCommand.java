package dev.plex.extras.command;

import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.TFMExtras;
import dev.plex.extras.jumppads.JumpPads;
import dev.plex.extras.jumppads.Mode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JumpPadsCommand extends SimplePlexCommand
{
    public JumpPadsCommand()
    {
        super(command("jumppads")
                .description("Enables jump pads for yourself or another player. Mode types available: none, regular, enhanced, extreme")
                .usage("/jumppads <mode> [player]")
                .aliases("jp,pads,launchpads")
                .permission("plex.tfmextras.jumppads")
                .build());
    }
    JumpPads jumpPads = TFMExtras.getModule().jumpPads;

    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        if ((args.length < 1) || (args.length > 2))
        {
            return usage();
        }

        if (args.length == 1)
        {
            try
            {
                if (sender instanceof ConsoleCommandSender)
                {
                    return MiniMessage.miniMessage().deserialize("<red>You must specify a player when running this command from console.");
                }

                if (player == null)
                {
                    return null;
                }

                if (args[0].equalsIgnoreCase("none") || args[0].equalsIgnoreCase("off"))
                {
                    jumpPads.removePlayer(player);
                    return MiniMessage.miniMessage().deserialize("<gray>You have disabled your jump pads.");
                }

                Mode mode = Mode.valueOf(args[0].toUpperCase());

                if (jumpPads.get(player) != null)
                {
                    if (mode.equals(jumpPads.get(player)))
                    {
                        return MiniMessage.miniMessage().deserialize("<red>Your jump pads are already set to " + mode.name() + ".");
                    }
                    else
                    {
                        jumpPads.updatePlayer(player, mode);
                        return MiniMessage.miniMessage().deserialize("<aqua>Successfully set your jump pads to " + mode.name() + ".");
                    }
                }

                jumpPads.addPlayer(player, mode);
                return MiniMessage.miniMessage().deserialize("<aqua>Successfully set your jump pads to " + mode.name() + ".");
            }
            catch (IllegalArgumentException ignored)
            {
                return MiniMessage.miniMessage().deserialize("<red>That is not a valid mode.");
            }
        }
        try
        {
            Player p = Bukkit.getPlayer(args[1]);

            if (p == null)
            {
                return MiniMessage.miniMessage().deserialize("<red>That player cannot be found.");
            }

            if (args[0].equalsIgnoreCase("none"))
            {
                jumpPads.removePlayer(p);
                return MiniMessage.miniMessage().deserialize("<gray>Jump pads for " + p.getName() + " have been disabled.");
            }

            Mode mode = Mode.valueOf(args[0]);

            if (!checkPermission(sender, "plex.tfmextras.jumppads.others"))
            {
                return permissionMessage();
            }

            if (jumpPads.get(p) != null)
            {
                if (jumpPads.get(p).equals(mode))
                {
                    return MiniMessage.miniMessage().deserialize("<red>Your jump pads are already set to " + mode.name() + ".");
                }

                jumpPads.updatePlayer(p, mode);
                return MiniMessage.miniMessage().deserialize("<gray>Jump pads for " + p.getName() + " have been set to " + mode.name() + ".");
            }

            jumpPads.addPlayer(p, mode);
            return MiniMessage.miniMessage().deserialize("<gray>Jump pads for " + p.getName() + " have been set to " + mode.name() + ".");
        }
        catch (IllegalArgumentException ignored)
        {
            return MiniMessage.miniMessage().deserialize("That is not a valid mode.");
        }
    }

    @Override
    protected @NotNull List<String> suggestions(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        if (silentCheckPermission(sender, this.getPermission()))
        {
            if (args.length == 1)
            {
                return Arrays.asList("none", "normal", "enhanced", "extreme");
            }
            else if (args.length == 2)
            {
                return onlinePlayerNames();
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
