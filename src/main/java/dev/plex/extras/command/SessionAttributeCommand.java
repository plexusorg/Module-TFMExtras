package dev.plex.extras.command;

import com.mojang.brigadier.context.CommandContext;
import dev.plex.command.CommandSpec;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.fun.SessionAttributes;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public abstract class SessionAttributeCommand extends SimplePlexCommand
{
    private final SessionAttributes attributes;
    private final Attribute attribute;
    private final String setKey;
    private final String resetKey;

    protected SessionAttributeCommand(CommandSpec commandSpec, SessionAttributes attributes, Attribute attribute,
                                      String setKey, String resetKey)
    {
        super(commandSpec);
        this.attributes = attributes;
        this.attribute = attribute;
        this.setKey = setKey;
        this.resetKey = resetKey;
    }

    protected List<String> targets(CommandContext<CommandSourceStack> context)
    {
        CommandSender sender = context.getSource().getSender();
        if (sender.hasPermission(getPermission() + ".others")) return onlinePlayerNames();
        return sender instanceof Player player ? List.of(player.getName()) : List.of();
    }

    protected @Nullable Component apply(CommandSender sender, @Nullable Player player, String name, @Nullable Double value)
    {
        Player target = target(sender, player, name);
        if (target == null) return messageComponent("playerNotFound");

        Component success = value == null
                ? messageComponent(resetKey, Placeholder.unparsed("player", target.getName()))
                : messageComponent(setKey, Placeholder.unparsed("player", target.getName()),
                        Placeholder.unparsed("value", String.valueOf(value)));
        Runnable mutation = value == null
                ? () -> attributes.reset(target, attribute)
                : () -> attributes.set(target, attribute, value);

        if (target == player)
        {
            mutation.run();
            return success;
        }

        Component unavailable = messageComponent("funPlayerUnavailable", Placeholder.unparsed("player", target.getName()));
        // Cross only for the attribute mutation; report it after the target's region applies it.
        if (ownTask(target.getScheduler().run(taskOwner(), task ->
        {
            mutation.run();
            sender.sendMessage(success);
        }, () -> sender.sendMessage(unavailable))) == null)
        {
            sender.sendMessage(unavailable);
        }
        return null;
    }

    private @Nullable Player target(CommandSender sender, @Nullable Player player, String name)
    {
        Player target = Bukkit.getPlayerExact(name);
        if (target != null && target != player) checkPermission(sender, getPermission() + ".others");
        return target;
    }
}
