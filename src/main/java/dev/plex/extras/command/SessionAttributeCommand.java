package dev.plex.extras.command;

import dev.plex.api.message.ActionBroadcast;
import dev.plex.command.CommandSpec;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.fun.SessionAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
    private final String setEveryoneKey;
    private final String resetEveryoneKey;

    protected SessionAttributeCommand(CommandSpec commandSpec, SessionAttributes attributes, Attribute attribute,
                                      String setKey, String resetKey, String setEveryoneKey, String resetEveryoneKey)
    {
        super(commandSpec);
        this.attributes = attributes;
        this.attribute = attribute;
        this.setKey = setKey;
        this.resetKey = resetKey;
        this.setEveryoneKey = setEveryoneKey;
        this.resetEveryoneKey = resetEveryoneKey;
    }

    protected @Nullable Component apply(CommandSender sender, @Nullable String name, @Nullable Double value)
    {
        List<Player> targets = resolveTargets(sender, name, getPermission() + ".others");
        if (!ALL_TARGETS.equals(name))
        {
            return applyTo(sender, targets.get(0), value);
        }

        ActionBroadcast announcement = api().messages().captureActionBroadcast(sender);
        AtomicBoolean announced = new AtomicBoolean();
        Component everyone = messageComponent(value == null ? resetEveryoneKey : setEveryoneKey,
                Placeholder.unparsed("sender", sender.getName()), Placeholder.unparsed("value", String.valueOf(value)));
        Runnable done = () ->
        {
            if (announced.compareAndSet(false, true)) announcement.send(everyone);
        };
        for (Player target : targets)
        {
            mutate(sender, target, value, done);
        }
        return null;
    }

    private @Nullable Component applyTo(CommandSender sender, Player target, @Nullable Double value)
    {
        Component success = value == null
                ? messageComponent(resetKey, Placeholder.unparsed("player", target.getName()))
                : messageComponent(setKey, Placeholder.unparsed("player", target.getName()),
                        Placeholder.unparsed("value", String.valueOf(value)));
        mutate(sender, target, value, () -> sender.sendMessage(success));
        return null;
    }

    // Crosses only for another player's attribute mutation; a self target already owns its own region.
    private void mutate(CommandSender sender, Player target, @Nullable Double value, Runnable done)
    {
        if (target.equals(sender))
        {
            mutation(target, value).run();
            done.run();
            return;
        }

        Component unavailable = messageComponent("funPlayerUnavailable", Placeholder.unparsed("player", target.getName()));
        if (ownTask(target.getScheduler().run(taskOwner(), task ->
        {
            mutation(target, value).run();
            done.run();
        }, () -> sender.sendMessage(unavailable))) == null)
        {
            sender.sendMessage(unavailable);
        }
    }

    private Runnable mutation(Player target, @Nullable Double value)
    {
        return value == null ? () -> attributes.reset(target, attribute) : () -> attributes.set(target, attribute, value);
    }
}
