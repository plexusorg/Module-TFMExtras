package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, this::executeTyped));
        command.then(greedyString("ignored").executes(context -> executeCommand(context, this::executeTyped)));
    }

    private Component executeTyped(CommandSender sender, Player player)
    {
        String senderName = sender.getName();
        List<org.bukkit.entity.Entity> clouds = Bukkit.getWorlds().stream()
                .map(World::getEntities)
                .flatMap(Collection::stream)
                .filter(entity -> entity.getType() == EntityType.AREA_EFFECT_CLOUD)
                .toList();
        clouds.forEach(entity -> scheduler().runEntity(entity, entity::remove));
        broadcast(messageComponent("areaEffectCloudClear", senderName));
        send(sender, messageComponent("areaEffectCloudsRemoved", clouds.size()));
        return null;
    }

}
