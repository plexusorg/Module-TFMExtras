package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.api.message.ActionBroadcast;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.TFMExtras;
import dev.plex.extras.jumppads.JumpPads;
import dev.plex.extras.jumppads.Mode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class JumpPadsCommand extends SimplePlexCommand
{
    private static final String OTHERS_PERMISSION = "plex.tfmextras.jumppads.others";

    private final JumpPads jumpPads;

    public JumpPadsCommand(TFMExtras module)
    {
        super(command("jumppads")
                .description("Enables jump pads for yourself, another player, or everyone. Mode types available: none, regular, enhanced, extreme")
                .usage("/<command> <mode> [player | -a]")
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
                .executes(context -> executeCommand(context, (sender, player) -> execute(sender, string(context, "mode"), null)))
                .then(targetArgument("player", OTHERS_PERMISSION)
                        .executes(context -> executeCommand(context, (sender, player) -> execute(sender,
                                string(context, "mode"), string(context, "player"))))));
    }

    private Component execute(CommandSender sender, String modeName, @Nullable String name)
    {
        boolean disable = modeName.equalsIgnoreCase("none") || modeName.equalsIgnoreCase("off");
        Mode mode = null;
        if (!disable)
        {
            try
            {
                mode = Mode.valueOf(modeName.toUpperCase());
            }
            catch (IllegalArgumentException ignored)
            {
                return messageComponent("jumpPadsInvalidMode");
            }
        }

        List<Player> targets = resolveTargets(sender, name, OTHERS_PERMISSION);
        if (!ALL_TARGETS.equals(name))
        {
            return applyTo(targets.get(0), disable, mode, name == null);
        }

        Mode everyoneMode = mode;
        ActionBroadcast announcement = api().messages().captureActionBroadcast(sender);
        AtomicBoolean announced = new AtomicBoolean();
        Component everyone = messageComponent(disable ? "jumpPadsDisabledEveryone" : "jumpPadsSetEveryone",
                Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("mode", disable ? "" : everyoneMode.name()));
        Runnable done = () ->
        {
            if (announced.compareAndSet(false, true)) announcement.send(everyone);
        };
        for (Player target : targets)
        {
            if (disable)
            {
                jumpPads.removePlayer(target);
                done.run();
            }
            else if (!everyoneMode.equals(jumpPads.get(target)))
            {
                jumpPads.setMode(target, everyoneMode);
                done.run();
            }
        }
        return null;
    }

    private Component applyTo(Player target, boolean disable, @Nullable Mode mode, boolean self)
    {
        if (disable)
        {
            jumpPads.removePlayer(target);
            return messageComponent(self ? "jumpPadsDisabledSelf" : "jumpPadsDisabledOther",
                    Placeholder.parsed("player", target.getName()));
        }

        if (mode.equals(jumpPads.get(target)))
        {
            return messageComponent("jumpPadsAlreadySet", Placeholder.unparsed("mode", mode.name()));
        }

        jumpPads.setMode(target, mode);
        return messageComponent(self ? "jumpPadsSetSelf" : "jumpPadsSetOther",
                Placeholder.unparsed("mode", mode.name()), Placeholder.parsed("player", target.getName()));
    }
}
