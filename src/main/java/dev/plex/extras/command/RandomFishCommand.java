package dev.plex.extras.command;

import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Bukkit;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import dev.plex.command.source.RequiredCommandSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
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
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, this::executeTyped));
        command.then(greedyString("ignored").executes(context -> executeCommand(context, this::executeTyped)));
    }

    private Component executeTyped(CommandSender sender, Player player)
    {
        @Nullable Block block = player.getTargetBlockExact(15);
        if (block == null)
        {
            return MiniMessage.miniMessage().deserialize("<red>There is no block within 15 blocks of you.");
        }
        Location location = block.getLocation().add(0, 1, 0);
        ownTask(Bukkit.getRegionScheduler().run(taskOwner(), location,
                task -> location.getWorld().spawnEntity(location, randomFish())));
        return MiniMessage.miniMessage().deserialize(":goodbird:");
    }

    private EntityType randomFish()
    {
        return FISH_TYPES.get(ThreadLocalRandom.current().nextInt(FISH_TYPES.size()));
    }

}
