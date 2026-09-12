package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.api.message.ActionBroadcast;
import dev.plex.command.SimplePlexCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CookieCommand extends SimplePlexCommand
{
    private static final ItemStack COOKIE = cookie();

    public CookieCommand()
    {
        super(command("cookie")
                .description("Gives a cookie to everyone on the server")
                .permission("plex.tfmextras.cookie")
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
        ActionBroadcast announcement = api().messages().captureActionBroadcast(sender);
        ownTask(Bukkit.getGlobalRegionScheduler().run(taskOwner(), task ->
        {
            List.copyOf(Bukkit.getOnlinePlayers()).forEach(
                    target -> ownTask(target.getScheduler().run(taskOwner(), ignored ->
                            target.getInventory().addItem(COOKIE.clone()), null)));
            announcement.send(mmString("<rainbow>Cookies are here! Om nom nom."));
        }));
        return null;
    }

    private static ItemStack cookie()
    {
        ItemStack cookie = new ItemStack(Material.COOKIE);
        ItemMeta meta = cookie.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize("<!italic><gold>Cookie"));
        cookie.setItemMeta(meta);
        return cookie;
    }

}
