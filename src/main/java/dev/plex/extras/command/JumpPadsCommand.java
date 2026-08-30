package dev.plex.extras.command;

import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.TFMExtras;
import dev.plex.extras.jumppads.JumpPads;
import dev.plex.extras.jumppads.Mode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JumpPadsCommand extends SimplePlexCommand
{
    private final JumpPads jumpPads;

    public JumpPadsCommand(TFMExtras module)
    {
        super(command("jumppads")
                .description("Enables jump pads for yourself or another player. Mode types available: none, regular, enhanced, extreme")
                .usage("/jumppads <mode> [player]")
                .aliases("jp,pads,launchpads")
                .permission("plex.tfmextras.jumppads")
                .build());
        this.jumpPads = module.getJumpPads();
    }

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
                    return messageComponent("jumpPadsConsoleSpecifyPlayer");
                }

                if (player == null)
                {
                    return null;
                }

                if (args[0].equalsIgnoreCase("none") || args[0].equalsIgnoreCase("off"))
                {
                    jumpPads.removePlayer(player);
                    return messageComponent("jumpPadsDisabledSelf");
                }

                Mode mode = Mode.valueOf(args[0].toUpperCase());

                if (mode.equals(jumpPads.get(player)))
                {
                    return messageComponent("jumpPadsAlreadySet", mode.name());
                }

                jumpPads.setMode(player, mode);
                return messageComponent("jumpPadsSetSelf", mode.name());
            }
            catch (IllegalArgumentException ignored)
            {
                return messageComponent("jumpPadsInvalidMode");
            }
        }
        try
        {
            Player p = Bukkit.getPlayer(args[1]);

            if (p == null)
            {
                return messageComponent("jumpPadsPlayerNotFound");
            }

            if (args[0].equalsIgnoreCase("none"))
            {
                jumpPads.removePlayer(p);
                return messageComponent("jumpPadsDisabledOther", p.getName());
            }

            Mode mode = Mode.valueOf(args[0]);

            if (!checkPermission(sender, "plex.tfmextras.jumppads.others"))
            {
                return permissionMessage();
            }

            if (mode.equals(jumpPads.get(p)))
            {
                return messageComponent("jumpPadsAlreadySet", mode.name());
            }

            jumpPads.setMode(p, mode);
            return messageComponent("jumpPadsSetOther", p.getName(), mode.name());
        }
        catch (IllegalArgumentException ignored)
        {
            return messageComponent("jumpPadsInvalidMode");
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
