package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.fun.Cages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UncageCommand extends SimplePlexCommand
{
    private final Cages cages;

    public UncageCommand(Cages cages)
    {
        super(command("uncage")
                .description("Removes a player's cage and restores the previous blocks")
                .usage("/<command> <player>")
                .permission("plex.tfmextras.cage")
                .build());
        this.cages = cages;
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> usage()));
        command.then(word("player").suggests((context, builder) -> suggestMatching(builder, onlinePlayerNames()))
                .executes(context -> executeCommand(context,
                        (sender, player) -> remove(sender, string(context, "player")))));
    }

    private Component remove(CommandSender sender, String name)
    {
        Player target = getNonNullPlayer(name);
        if (!cages.uncage(target.getUniqueId()))
        {
            return messageComponent("cageNotCaged", Placeholder.unparsed("player", target.getName()));
        }

        Bukkit.broadcast(messageComponent("cageRemoved", Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("player", target.getName())));
        return null;
    }

}
