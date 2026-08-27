package dev.plex.extras.command;

import dev.plex.command.SimplePlexCommand;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        AtomicInteger removed = new AtomicInteger();
        Bukkit.getWorlds().stream().map(World::getEntities).flatMap(Collection::stream).filter(entity -> entity.getType() == EntityType.AREA_EFFECT_CLOUD).forEach(entity ->
        {
            removed.incrementAndGet();
            scheduler().runEntity(entity, entity::remove);
        });
        broadcast(messageComponent("areaEffectCloudClear", sender.getName()));
        return messageComponent("areaEffectCloudsRemoved", removed.get());
    }

    @Override
    protected @NotNull List<String> suggestions(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        return Collections.emptyList();
    }
}
