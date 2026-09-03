package dev.plex.extras.command;

import net.kyori.adventure.text.minimessage.MiniMessage;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import dev.plex.extras.TFMExtras;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdminInfoCommand extends SimplePlexCommand
{
    private final TFMExtras module;

    public AdminInfoCommand(TFMExtras module)
    {
        super(command("admininfo")
                .description("Information on how to apply for admin")
                .aliases("ai,si,staffinfo")
                .permission("plex.tfmextras.admininfo")
                .build());
        this.module = module;
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, this::executeTyped));
        command.then(greedyString("ignored").executes(context -> executeCommand(context, this::executeTyped)));
    }

    private Component executeTyped(CommandSender sender, Player player)
    {
        List<Component> adminInfo = module.getConfig().getStringList("server.admininfo")
                .stream().map(info -> MiniMessage.miniMessage().deserialize(info)).toList();
        if (adminInfo.isEmpty())
        {
            return messageComponent("emptyAdminInfo");
        }
        adminInfo.forEach(component -> send(sender, component));
        return null;
    }

}
