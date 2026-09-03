package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.TFMExtras;
import dev.plex.extras.jumppads.JumpPads;
import dev.plex.extras.jumppads.Mode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
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
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> usage()));
        command.then(word("mode").suggests((context, builder) -> suggestMatching(builder, List.of("none", "normal", "enhanced", "extreme")))
                .executes(context -> executeCommand(context, (sender, player) -> executeTyped(sender, player, string(context, "mode"), null)))
                .then(word("target").suggests((context, builder) -> suggestMatching(builder, onlinePlayerNames()))
                        .executes(context -> executeCommand(context, (sender, player) -> executeTyped(sender, player, string(context, "mode"), string(context, "target"))))
                        .then(greedyString("extra").executes(context -> executeCommand(context, (sender, player) -> usage())))));
    }

    private Component executeTyped(CommandSender sender, Player player, String modeName, @Nullable String targetName)
    {
        if (targetName == null)
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

                if (modeName.equalsIgnoreCase("none") || modeName.equalsIgnoreCase("off"))
                {
                    jumpPads.removePlayer(player);
                    return messageComponent("jumpPadsDisabledSelf");
                }

                Mode mode = Mode.valueOf(modeName.toUpperCase());

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
        checkPermission(sender, "plex.tfmextras.jumppads.others");
        try
        {
            Player p = getNonNullPlayer(targetName);

            if (modeName.equalsIgnoreCase("none"))
            {
                jumpPads.removePlayer(p);
                return messageComponent("jumpPadsDisabledOther", p.getName());
            }

            Mode mode = Mode.valueOf(modeName.toUpperCase());

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

}
