package dev.plex.extras.command;

import dev.plex.command.SimplePlexCommand;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CakeCommand extends SimplePlexCommand
{
    public CakeCommand()
    {
        super(command("cake")
                .description("For the people that are still alive - gives a cake to everyone on the server")
                .permission("plex.tfmextras.cake")
                .build());
    }
    private static final ItemStack CAKE = cake();

    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        Bukkit.getOnlinePlayers().forEach(p -> api().scheduler().runEntity(p, () -> p.getInventory().addItem(CAKE.clone())));
        broadcast("<rainbow>But there's no sense crying over every mistake. You just keep on trying till you run out of cake.");
        return null;
    }

    private static ItemStack cake()
    {
        ItemStack cake = new ItemStack(Material.CAKE);
        ItemMeta meta = cake.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize("<!italic><white>The <dark_gray>Lie"));
        cake.setItemMeta(meta);
        return cake;
    }

    @Override
    protected @NotNull List<String> suggestions(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) throws IllegalArgumentException
    {
        return Collections.emptyList();
    }
}
