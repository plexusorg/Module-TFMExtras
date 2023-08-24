package dev.plex.extras;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import dev.plex.extras.hook.SlimeWorldHook;
import dev.plex.extras.listener.PlayerListener;
import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.config.ModuleConfig;
import dev.plex.extras.jumppads.JumpPads;
import dev.plex.extras.listener.JumpPadsListener;
import dev.plex.listener.PlexListener;
import dev.plex.module.PlexModule;
import dev.plex.util.PlexLog;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class TFMExtras extends PlexModule
{
    @Getter
    private static TFMExtras module;

    public JumpPads jumpPads;

    @Getter
    private ModuleConfig config;

    @Getter
    private final SlimeWorldHook slimeWorldHook = new SlimeWorldHook();

    @Override
    public void load()
    {
        module = this;
        config = new ModuleConfig(this, "tfmextras/config.yml", "config.yml");
        config.load();
        jumpPads = new JumpPads();
        PlexLog.debug(String.valueOf(config.getInt("server.jumppad_strength")));
    }

    @Override
    public void enable()
    {
        if (slimeWorldHook.plugin() != null)
        {
            slimeWorldHook.onEnable(this);
        }

        getClassesFrom("dev.plex.extras.command").forEach(aClass ->
        {
            if (PlexCommand.class.isAssignableFrom(aClass) && aClass.isAnnotationPresent(CommandParameters.class) && aClass.isAnnotationPresent(CommandPermissions.class))
            {
                try
                {
                    PlexCommand plexCommand = (PlexCommand) aClass.getConstructors()[0].newInstance();
                    registerCommand(plexCommand);
                }
                catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        getClassesFrom("dev.plex.extras.listener").forEach(aClass ->
        {
            if (PlexListener.class.isAssignableFrom(aClass))
            {
                try
                {
                    PlexListener plexListener = (PlexListener) aClass.getConstructors()[0].newInstance();
                    registerListener(plexListener);
                }
                catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addDefaultMessage("emptyAdminInfo", "<red>The admin information section of the config.yml file has not been configured.");
        addDefaultMessage("cakeLyrics", "<rainbow>But there's no sense crying over every mistake. You just keep on trying till you run out of cake.");
        addDefaultMessage("areaEffectCloudClear", "<red>{0} - Removing all area effect clouds", "0 - The command sender");
        addDefaultMessage("chatCleared", "<red>{0} - Cleared the chat", "0 - The command sender");
        addDefaultMessage("attributeList", "<gold>All possible attributes: <yellow>{0}", "0 - The attribute list, each split by a new line");
        addDefaultMessage("modifiedAutoClear", "<gold>{0} will {1} have their inventory cleared when they join.", "0 - The player who will have their inventory cleared on join", "1 - Whether they had this option toggled (returns: 'no longer', 'now')");
        addDefaultMessage("modifiedAutoTeleport", "<gold>{0} will {1} be teleported automatically when they join.", "0 - The player to be teleported automatically", "1 - Whether they had this option toggled (returns: 'no longer', 'now')");
        addDefaultMessage("createdPlayerWorld", "<green>Welcome to the server! We've created you a new private world where you can invite your friends! View how to use this using /myworld!");
    }

    @Override
    public void disable()
    {
        // Unregistering listeners / commands is handled by Plex
        if (slimeWorldHook.plugin() != null)
        {
            slimeWorldHook.onDisable(this);
        }
    }

    public static Location getRandomLocation(World world)
    {
        double x = ThreadLocalRandom.current().nextDouble(-100000, 100000);
        double z = ThreadLocalRandom.current().nextDouble(-100000, 100000);
        double y = world.getHighestBlockYAt((int) x, (int) z) + 1;
        return new Location(world, x, y, z);
    }

    private Set<Class<?>> getClassesFrom(String packageName)
    {
        Set<Class<?>> classes = new HashSet();

        try
        {
            ClassPath path = ClassPath.from(TFMExtras.class.getClassLoader());
            ImmutableSet<ClassPath.ClassInfo> infoSet = path.getTopLevelClasses(packageName);
            infoSet.forEach((info) ->
            {
                try
                {
                    Class<?> clazz = Class.forName(info.getName());
                    classes.add(clazz);
                }
                catch (ClassNotFoundException var4)
                {
                    PlexLog.error("Unable to find class " + info.getName() + " in " + packageName);
                }

            });
        }
        catch (IOException var4)
        {
            PlexLog.error("Something went wrong while fetching classes from " + packageName);
            throw new RuntimeException(var4);
        }

        return Collections.unmodifiableSet(classes);
    }
}