package dev.plex.extras.command;

import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.util.PlexUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandParameters(name = "cloudclear", description = "Clears lingering potion area effect clouds", aliases = "clearcloud,aeclear")
@CommandPermissions(permission = "plex.tfmextras.cloudclear")
public class CloudClearCommand extends PlexCommand
{
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        AtomicInteger removed = new AtomicInteger();
        Bukkit.getWorlds().stream().map(World::getEntities).flatMap(Collection::stream).filter(entity -> entity.getType() == EntityType.AREA_EFFECT_CLOUD).peek(entity ->
        {
            entity.remove();
            removed.incrementAndGet();
        });
        PlexUtils.broadcast(messageComponent("areaEffectCloudClear", sender.getName()));
        return MiniMessage.miniMessage().deserialize("<gray>" + removed.get() + " area effect clouds removed.");
    }

    @Override
    public @NotNull List<String> smartTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        return Collections.emptyList();
    }
}
