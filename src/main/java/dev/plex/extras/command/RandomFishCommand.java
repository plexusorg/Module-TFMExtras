package dev.plex.extras.command;

import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.command.source.RequiredCommandSource;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@CommandParameters(name = "randomfish", description = "Spawns a random type of fish at your location", aliases = "rfish,bird")
@CommandPermissions( permission = "plex.tfmextras.randomfish", source = RequiredCommandSource.IN_GAME)
public class RandomFishCommand extends PlexCommand
{
    private static final List<EntityType> FISH_TYPES = Arrays.asList(EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH);

    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        @Nullable Block block = player.getTargetBlockExact(15);
        if (block == null)
        {
            return MiniMessage.miniMessage().deserialize("<red>There is no block within 15 blocks of you.");
        }
        player.getWorld().spawnEntity(block.getLocation().add(0, 1, 0), randomFish());
        return MiniMessage.miniMessage().deserialize(":goodbird:");
    }

    private EntityType randomFish()
    {
        return FISH_TYPES.get(ThreadLocalRandom.current().nextInt(FISH_TYPES.size()));
    }
}
