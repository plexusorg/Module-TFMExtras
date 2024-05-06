package dev.plex.extras.command.slime;

import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.command.source.RequiredCommandSource;
import dev.plex.extras.TFMExtras;
import dev.plex.util.PlexLog;
import dev.plex.util.PlexUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandParameters(name = "slimemanager", usage = "/<command> <delete | list> [world | all]", description = "Manages the slime worlds handled by the plugin")
@CommandPermissions(source = RequiredCommandSource.CONSOLE, permission = "plex.tfmextras.slimemanager")
public class SlimeManagerCommand extends PlexCommand
{
    private ScheduledTask task = null;

    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        if (args.length == 0)
        {
            return usage();
        }
        if (!TFMExtras.getModule().enableIslands())
        {
            return mmString("<red>SlimeWorldManager is not on this server!");
        }
        try
        {
            if (args[0].equalsIgnoreCase("delete"))
            {
                if (args.length != 2)
                {
                    return usage("/slimemanager delete <world | all>");
                }
                String argument = args[1];
                if (TFMExtras.getModule().getSlimeWorldHook().getLoader().listWorlds().isEmpty())
                {
                    return mmString("<red>There are currently no loaded worlds.");
                }
                else if (!argument.equalsIgnoreCase("all") && TFMExtras.getModule().getSlimeWorldHook().getLoader().listWorlds().stream().noneMatch(s -> s.equalsIgnoreCase(argument)))
                {
                    return mmString("<red>There is no world called " + argument);
                }
                if (task != null)
                {
                    if (argument.equalsIgnoreCase("all"))
                    {
                        TFMExtras.getModule().getSlimeWorldHook().getLoader().listWorlds().forEach(s ->
                                TFMExtras.getModule().getSlimeWorldHook().deleteWorld(s));
                        if (task != null && !task.isCancelled())
                        {
                            task.cancel();
                            task = null;
                        }
                        return mmString("<green>Successfully permanently deleted all slime module loaded worlds!");
                    }
                    else
                    {
                        TFMExtras.getModule().getSlimeWorldHook().deleteWorld(argument);
                        if (task != null && !task.isCancelled())
                        {
                            task.cancel();
                            task = null;
                        }
                        return mmString("<green>Successfully permanently deleted the world <dark_green>" + argument);
                    }
                }
                else
                {
                    task = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask ->
                    {
                        this.task = null;
                        sender.sendMessage(PlexUtils.mmDeserialize("<red>You did not confirm the deletion in time!"));
                    }, 10 * 20L);
                    return mmString("<green>Run this command again to confirm deletion.");
                }
            }
            else if (args[0].equalsIgnoreCase("list"))
            {
                return mmString("<blue>Current worlds: <white>" + StringUtils.join(TFMExtras.getModule().getSlimeWorldHook().getLoader().listWorlds(), ", "));
            }
        }
        catch (Exception e)
        {
            PlexLog.debug("{0}: {1}", e.getClass().getName(), e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    public @NotNull List<String> smartTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        if (silentCheckPermission(sender, this.getPermission()))
        {
            if (args.length == 1)
            {
                return Arrays.asList("delete", "list");
            }
            if (args.length == 2)
            {
                return TFMExtras.getModule().getSlimeWorldHook().loadedWorlds().stream().toList();
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
