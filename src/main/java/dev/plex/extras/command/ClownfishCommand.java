package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.api.player.PlexPlayerView;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.TFMExtras;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> executeTyped(sender, player, null, null)));
        command.then(word("action").suggests((context, builder) ->
        {
            List<String> actions = new java.util.ArrayList<>(List.of("toggle"));
            if (context.getSource().getSender().hasPermission("plex.tfmextras.clownfish.restrict")) actions.add("restrict");
            return suggestMatching(builder, actions);
        }).executes(context -> executeCommand(context, (sender, player) -> executeTyped(sender, player,
                        string(context, "action"), null)))
                .then(word("target").suggests((context, builder) ->
                {
                    return string(context, "action").equals("restrict")
                            ? suggestMatching(builder, onlinePlayerNames()) : builder.buildFuture();
                }).executes(context -> executeCommand(context, (sender, player) -> executeTyped(sender, player,
                                string(context, "action"), string(context, "target"))))
                        .then(greedyString("extra").executes(context -> executeCommand(context,
                                (sender, player) -> usage())))));
    }

    private Component executeTyped(CommandSender commandSender, Player player, @Nullable String action, @Nullable String target)
    {
        if (action == null)
        {
            ItemStack clownfish = new ItemStack(Material.TROPICAL_FISH);
            ItemMeta meta = clownfish.getItemMeta();

            meta.displayName(Component.text("Clownfish"));
            clownfish.setItemMeta(meta);

            player.getInventory().addItem(clownfish);
            return MiniMessage.miniMessage().deserialize("<rainbow>blub blub... ><_>");
        }
        else if (action.equals("toggle"))
        {
            module.toggleConfigEntry("server.clownfish.toggled_players", player.getUniqueId().toString())
                    .whenComplete((enabled, failure) ->
                    {
                        if (failure != null) module.getLogger().error("Failed to update clownfish toggle", failure);
                        else send(player, MiniMessage.miniMessage().deserialize("<gray>You will "
                                + (enabled ? "no longer" : "now") + " be affected by the clownfish."));
                    });
            return null;
        }
        else if (action.equals("restrict") && target != null)
        {
            if (silentCheckPermission(commandSender, "plex.tfmextras.clownfish.restrict"))
            {
                api().players().byName(target).whenComplete((result, failure) ->
                {
                    if (failure != null)
                    {
                        module.getLogger().error("Failed to look up player {}", target, failure);
                        send(commandSender, Component.text("Player lookup failed."));
                        return;
                    }
                    if (result.isEmpty())
                    {
                        send(commandSender, messageComponent("playerNotFound"));
                        return;
                    }
                    restrict(commandSender, result.get());
                });
                return null;
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

    private void restrict(CommandSender sender, PlexPlayerView target)
    {
        module.toggleConfigEntry("server.clownfish.restricted", target.uuid().toString())
                .whenComplete((restricted, failure) ->
                {
                    if (failure != null) module.getLogger().error("Failed to update clownfish restriction", failure);
                    else send(sender, MiniMessage.miniMessage().deserialize("<gold>" + target.name() + " will "
                            + (restricted ? "no longer" : "now") + " be able to use the clownfish."));
                });
    }

}
