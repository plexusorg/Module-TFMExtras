package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> usage()));
        command.then(word("player").suggests((context, builder) -> suggestMatching(builder, onlinePlayerNames()))
                .executes(context -> executeCommand(context,
                        (sender, player) -> executeTyped(sender, string(context, "player"))))
                .then(greedyString("ignored").executes(context -> executeCommand(context,
                        (sender, player) -> executeTyped(sender, string(context, "player"))))));
    }

    private Component executeTyped(CommandSender sender, String playerName)
    {
        Player target = getNonNullPlayer(playerName);
        target.sendMessage(mmString("<red>ENGLISH MOTHERFUCKER, Do you speak it!?"));
        scheduler().runEntity(target, () ->
        {
            target.setHealth(0);
            target.getWorld().strikeLightningEffect(target.getLocation());
        });
        broadcast("<red>" + sender.getName() + " is sick of " + target.getName() + " not speaking English!");
        return null;
    }

}
