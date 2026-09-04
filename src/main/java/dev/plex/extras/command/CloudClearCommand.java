package dev.plex.extras.command;

import org.bukkit.Bukkit;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.text.Component;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.plex.api.message.MessagePlaceholder.placeholder;

public class CloudClearCommand extends SimplePlexCommand
{
    public CloudClearCommand()
    {
        super(command("cloudclear")
                .description("Clears lingering potion area effect clouds")
                .aliases("clearcloud,aeclear")
                .permission("plex.tfmextras.cloudclear")
                .build());
    }
    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, this::executeTyped));
        command.then(greedyString("ignored").executes(context -> executeCommand(context, this::executeTyped)));
    }

    private Component executeTyped(CommandSender sender, Player player)
    {
        String senderName = sender.getName();
        ownTask(Bukkit.getGlobalRegionScheduler().run(taskOwner(), task ->
        {
            List<Chunk> chunks = new ArrayList<>();
            Bukkit.getWorlds().forEach(world -> chunks.addAll(List.of(world.getLoadedChunks())));
            if (chunks.isEmpty())
            {
                report(sender, senderName, 0);
                return;
            }
            AtomicInteger remaining = new AtomicInteger(chunks.size());
            AtomicInteger removed = new AtomicInteger();
            for (Chunk chunk : chunks)
            {
                ownTask(Bukkit.getRegionScheduler().run(taskOwner(), chunk.getWorld(), chunk.getX(), chunk.getZ(),
                        regionTask ->
                        {
                            for (org.bukkit.entity.Entity entity : chunk.getEntities())
                            {
                                if (entity.getType() != EntityType.AREA_EFFECT_CLOUD) continue;
                                entity.remove();
                                removed.incrementAndGet();
                            }
                            if (remaining.decrementAndGet() == 0) report(sender, senderName, removed.get());
                        }));
            }
        }));
        return null;
    }

    private void report(CommandSender sender, String senderName, int removed)
    {
        broadcast(messageComponent("areaEffectCloudClear", placeholder("sender", senderName)));
        send(sender, messageComponent("areaEffectCloudsRemoved", placeholder("count", removed)));
    }

}
