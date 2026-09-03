package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, this::executeTyped));
        command.then(greedyString("ignored").executes(context -> executeCommand(context, this::executeTyped)));
    }

    private Component executeTyped(CommandSender sender, Player player)
    {
        scheduler().runGlobal(() ->
        {
            List.copyOf(Bukkit.getOnlinePlayers()).forEach(
                    target -> scheduler().runEntity(target, () -> target.getInventory().addItem(CAKE.clone())));
            broadcast("<rainbow>But there's no sense crying over every mistake. You just keep on trying till you run out of cake.");
        });
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

}
