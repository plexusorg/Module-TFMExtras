package dev.plex.command;

import dev.plex.TFMExtras;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.rank.enums.Rank;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandParameters(name = "admininfo", description = "Information on how to apply for admin", aliases = "ai,si,staffinfo")
@CommandPermissions(level = Rank.OP, permission = "plex.tfmextras.admininfo")
public class AdminInfoCommand extends PlexCommand
{
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
}
