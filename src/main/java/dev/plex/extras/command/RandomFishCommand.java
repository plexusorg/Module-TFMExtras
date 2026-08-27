package dev.plex.extras.command;

import dev.plex.command.SimplePlexCommand;
import dev.plex.command.source.RequiredCommandSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RandomFishCommand extends SimplePlexCommand
{
    public RandomFishCommand()
    {
        super(command("randomfish")
                .description("Spawns a random type of fish at your location")
                .aliases("rfish,bird")
                .permission("plex.tfmextras.randomfish")
                .source(RequiredCommandSource.IN_GAME)
                .build());
    }
    private static final List<EntityType> FISH_TYPES = Arrays.asList(EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH);

    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        @Nullable Block block = player.getTargetBlockExact(15);
        if (block == null)
        {
            return MiniMessage.miniMessage().deserialize("<red>There is no block within 15 blocks of you.");
        }
        Location location = block.getLocation().add(0, 1, 0);
        scheduler().runRegion(location, () -> location.getWorld().spawnEntity(location, randomFish()));
        return MiniMessage.miniMessage().deserialize(":goodbird:");
    }

    private EntityType randomFish()
    {
        return FISH_TYPES.get(ThreadLocalRandom.current().nextInt(FISH_TYPES.size()));
    }

    @Override
    protected @NotNull List<String> suggestions(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        return Collections.emptyList();
    }
}
