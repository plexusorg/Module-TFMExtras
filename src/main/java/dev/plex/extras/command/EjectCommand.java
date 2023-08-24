package dev.plex.extras.command;

import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.command.source.RequiredCommandSource;
import dev.plex.rank.enums.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandParameters(name = "eject", description = "Removes all passengers from a player")
@CommandPermissions(level = Rank.OP, permission = "plex.tfmextras.eject", source = RequiredCommandSource.IN_GAME)
public class EjectCommand extends PlexCommand
{
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        final int passengers = player.getPassengers().size();
        player.eject();
        return MiniMessage.miniMessage().deserialize("<gray>Ejected " + passengers + " passengers.");
    }
}
