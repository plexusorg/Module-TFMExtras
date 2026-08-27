package dev.plex.extras.command;

import dev.plex.api.player.PlexPlayerView;
import dev.plex.command.SimplePlexCommand;
import dev.plex.command.exception.PlayerNotFoundException;
import dev.plex.extras.TFMExtras;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClownfishCommand extends SimplePlexCommand
{
    private final TFMExtras module;

    public ClownfishCommand(TFMExtras module)
    {
        super(command("clownfish")
                .description("Gives a player a clownfish capable of knocking people back")
                .usage("/<command> [<toggle>]")
                .permission("plex.tfmextras.clownfish")
                .build());
        this.module = module;
    }

    @Override
    protected Component execute(@NotNull CommandSender commandSender, @Nullable Player player, @NotNull String[] args)
    {
        if (args.length == 0)
        {
            ItemStack clownfish = new ItemStack(Material.TROPICAL_FISH);
            ItemMeta meta = clownfish.getItemMeta();

            meta.displayName(Component.text("Clownfish"));
            clownfish.setItemMeta(meta);

            scheduler().runEntity(player, () -> player.getInventory().addItem(clownfish));
            return MiniMessage.miniMessage().deserialize("<rainbow>blub blub... ><_>");
        }
        else if (args[0].equals("toggle"))
        {
            List<String> toggledPlayers = module.getConfig().getStringList("server.clownfish.toggled_players");

            boolean isToggled = toggledPlayers.contains(player.getUniqueId().toString());
            if (isToggled)
            {
                toggledPlayers.remove(player.getUniqueId().toString());
            }
            else
            {
                toggledPlayers.add(player.getUniqueId().toString());
            }

            module.getConfig().set("server.clownfish.toggled_players", toggledPlayers);
            module.getConfig().save();

            return MiniMessage.miniMessage().deserialize("<gray>You will " + (isToggled ? "now" : "no longer") + " be affected by the clownfish.");
        }
        else if (args[0].equals("restrict") && args.length == 2)
        {
            if (silentCheckPermission(commandSender, "plex.tfmextras.clownfish.restrict"))
            {
                PlexPlayerView target = api().players().byName(args[1]).orElseThrow(PlayerNotFoundException::new);

                List<String> restrictedPlayers = module.getConfig().getStringList("server.clownfish.restricted");

                boolean isRestricted = restrictedPlayers.contains(target.uuid().toString());
                if (isRestricted)
                {
                    restrictedPlayers.remove(target.uuid().toString());
                }
                else
                {
                    restrictedPlayers.add(target.uuid().toString());
                }

                module.getConfig().set("server.clownfish.restricted", restrictedPlayers);
                module.getConfig().save();

                return MiniMessage.miniMessage().deserialize("<gold>" + target.name() + " will " + (isRestricted ? "now" : "no longer") + " be able to use the clownfish.");
            }
            else
            {
                return MiniMessage.miniMessage().deserialize("<red>You do not have permission to use this command.");
            }
        }
        else
        {
            return usage();
        }
    }

    @Override
    protected @NotNull List<String> suggestions(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (silentCheckPermission(sender, "plex.tfmextras.clownfish.restrict"))
        {
            if (args.length == 1)
            {
                return Arrays.asList("toggle", "restrict");
            }
            else if (args.length == 2 && args[0].equals("restrict"))
            {
                return onlinePlayerNames();
            }
        }
        else if (args.length == 1)
        {
            return List.of("toggle");
        }

        return Collections.emptyList();
    }
}
