package dev.plex.command;

import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.rank.enums.Rank;
import java.util.Arrays;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandParameters(name = "attributes", description = "Lists all possible attributes", aliases = "attributelist,attrlist")
@CommandPermissions(level = Rank.OP, permission = "plex.tfmextras.attrlist")
public class AttributeListCommand extends PlexCommand
{
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        return messageComponent("attributeList", StringUtils.join(Arrays.stream(Attribute.values()).map(Enum::name).toList(), ", "));
    }
}
