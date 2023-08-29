package dev.plex.extras.command;

import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.util.PlexUtils;
import dev.plex.util.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandParameters(name = "cake", description = "For the people that are still alive - gives a cake to everyone on the server")
@CommandPermissions(permission = "plex.tfmextras.cake")
public class CakeCommand extends PlexCommand
{
    private static final ItemStack CAKE = new ItemBuilder(Material.CAKE)
            .displayName(MiniMessage.miniMessage().deserialize("<!italic><white>The <dark_gray>Lie"))
            .build();

    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        Bukkit.getOnlinePlayers().forEach(p ->
        {
            p.getInventory().addItem(CAKE);
        });
        PlexUtils.broadcast(messageComponent("cakeLyrics"));
        return null;
    }
}
