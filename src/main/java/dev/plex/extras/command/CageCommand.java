package dev.plex.extras.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.plex.api.message.ActionBroadcast;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.fun.Cages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CageCommand extends SimplePlexCommand
{
    private final Cages cages;

    public CageCommand(Cages cages)
    {
        super(command("cage")
                .description("Traps a player in a cage until it is removed")
                .usage("/<command> <player> [off | <outer> [inner]]")
                .permission("plex.tfmextras.cage")
                .build());
        this.cages = cages;
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> usage()));
        command.then(word("player").suggests((context, builder) -> suggestMatching(builder, onlinePlayerNames()))
                .executes(context -> executeCommand(context, (sender, player) -> build(sender, string(context, "player"),
                        Material.GLASS.createBlockData(), Material.AIR.createBlockData())))
                .then(Commands.literal("off").executes(context -> executeCommand(context,
                        (sender, player) -> remove(sender, string(context, "player")))))
                .then(Commands.argument("outer", ArgumentTypes.blockState())
                        .executes(context -> executeCommand(context, (sender, player) -> build(sender, string(context, "player"),
                                blockData(context, "outer"), Material.AIR.createBlockData())))
                        .then(Commands.argument("inner", ArgumentTypes.blockState())
                                .executes(context -> executeCommand(context, (sender, player) -> build(sender, string(context, "player"),
                                        blockData(context, "outer"), blockData(context, "inner")))))));
    }

    private Component build(CommandSender sender, String name, BlockData outer, BlockData inner)
    {
        if (!safe(outer, false) || !safe(inner, true))
        {
            return messageComponent("cageUnsafeBlock");
        }

        Player target = getNonNullPlayer(name);
        ActionBroadcast announcement = api().messages().captureActionBroadcast(sender);
        Component built = messageComponent("cageBuilt", Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("player", target.getName()));
        Component unavailable = messageComponent("funPlayerUnavailable", Placeholder.unparsed("player", target.getName()));
        cages.cage(target, outer, inner, () -> announcement.send(built), () -> sender.sendMessage(unavailable));
        return null;
    }

    // Restoration only rewrites the recorded cube, so a block that falls, flows, or explodes must not
    // enter it. The fill may also be air or still water because the solid shell contains it.
    private boolean safe(BlockData data, boolean fill)
    {
        Material material = data.getMaterial();
        if (data instanceof Waterlogged waterlogged && waterlogged.isWaterlogged())
        {
            return false;
        }
        if (fill && (material.isAir() || material == Material.WATER))
        {
            return true;
        }
        return material.isSolid() && !material.hasGravity() && material != Material.TNT;
    }

    private Component remove(CommandSender sender, String name)
    {
        Player target = getNonNullPlayer(name);
        ActionBroadcast announcement = api().messages().captureActionBroadcast(sender);
        if (!cages.uncage(target.getUniqueId()))
        {
            return messageComponent("cageNotCaged", Placeholder.unparsed("player", target.getName()));
        }

        announcement.send(messageComponent("cageRemoved", Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("player", target.getName())));
        return null;
    }

    private BlockData blockData(CommandContext<CommandSourceStack> context, String name)
    {
        return context.getArgument(name, BlockState.class).getBlockData();
    }
}
