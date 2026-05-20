package dev.plex.extras.command;

import dev.plex.command.SimplePlexCommand;
import dev.plex.command.source.RequiredCommandSource;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EjectCommand extends SimplePlexCommand
{
    public EjectCommand()
    {
        super(command("eject")
                .description("Removes all passengers from a player")
                .permission("plex.tfmextras.eject")
                .source(RequiredCommandSource.IN_GAME)
                .build());
    }
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        final int passengers = player.getPassengers().size();
        api().scheduler().runEntity(player, player::eject);
        return messageComponent("passengersEjected", passengers);
    }

    @Override
    protected @NotNull List<String> suggestions(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        return Collections.emptyList();
    }
}
