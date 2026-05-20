package dev.plex.extras.command;

import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.TFMExtras;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdminInfoCommand extends SimplePlexCommand
{
    public AdminInfoCommand()
    {
        super(command("admininfo")
                .description("Information on how to apply for admin")
                .aliases("ai,si,staffinfo")
                .permission("plex.tfmextras.admininfo")
                .build());
    }
    private static final List<Component> ADMIN_INFO = TFMExtras.getModule().getConfig().getStringList("server.admininfo")
            .stream().map(info -> MiniMessage.miniMessage().deserialize(info)).toList();

    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        if (ADMIN_INFO.isEmpty())
        {
            return messageComponent("emptyAdminInfo");
        }
        ADMIN_INFO.forEach(component -> send(sender, component));
        return null;
    }

    @Override
    protected @NotNull List<String> suggestions(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        return Collections.emptyList();
    }
}
