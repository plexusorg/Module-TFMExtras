package dev.plex.extras;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import dev.plex.Plex;
import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.config.ModuleConfig;
import dev.plex.extras.jumppads.JumpPads;
import dev.plex.listener.PlexListener;
import dev.plex.module.PlexModule;
import dev.plex.util.PlexLog;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;

public class TFMExtras extends PlexModule
{
    @Getter
    private static TFMExtras module;

    public JumpPads jumpPads;

    @Getter
    private ModuleConfig config;

    @Override
    public void load()
    {
        module = this;
        config = new ModuleConfig(this, "tfmextras/config.yml", "config.yml");
        config.load();
        jumpPads = new JumpPads();
//        PlexLog.debug(String.valueOf(config.getInt("server.jumppad_strength")));
    }

    @Override
    public void enable()
    {
        getClassesFrom("dev.plex.extras.command").forEach(aClass ->
        {
            if (PlexCommand.class.isAssignableFrom(aClass) && aClass.isAnnotationPresent(CommandParameters.class) && aClass.isAnnotationPresent(CommandPermissions.class))
            {
                try
                {
                    PlexCommand plexCommand = (PlexCommand)aClass.getConstructors()[0].newInstance();
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
                    PlexListener plexListener = (PlexListener)aClass.getConstructors()[0].newInstance();
                    registerListener(plexListener);
                }
                catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addDefaultMessage("playerOrbited", "<aqua>{0} - Orbiting {1}", "0 - The command sender", "1 - The person being orbited");
        addDefaultMessage("stoppedOrbiting", "<aqua>No longer orbiting {0}", "0 - The person no longer being orbited");
        addDefaultMessage("alreadyOrbited", "<red>{0} is already being orbited!", "0 - The person that is already orbited");
        addDefaultMessage("restrictClownfish", "<gold>{0} will {1} be able to use the clownfish.", "0 - The player who will be restricted", "1 - Whether they had this option toggled (returns: 'no longer', 'now')");
        addDefaultMessage("toggleClownfish", "<gray>You will {0} be affected by the clownfish.", "0 - Whether they had this option toggled (returns: 'no longer', 'now')");
        addDefaultMessage("emptyAdminInfo", "<red>The admin information section of the config.yml file has not been configured.");
        addDefaultMessage("cakeLyrics", "<rainbow>But there's no sense crying over every mistake. You just keep on trying till you run out of cake.");
        addDefaultMessage("areaEffectCloudClear", "<red>{0} - Removing all area effect clouds", "0 - The command sender");
        addDefaultMessage("chatCleared", "<red>{0} - Cleared the chat", "0 - The command sender");
        addDefaultMessage("attributeList", "<gold>All possible attributes: <yellow>{0}", "0 - The attribute list, each split by a new line");
        addDefaultMessage("modifiedAutoClear", "<gold>{0} will {1} have their inventory cleared when they join.", "0 - The player who will have their inventory cleared on join", "1 - Whether they had this option toggled (returns: 'no longer', 'now')");
        addDefaultMessage("modifiedAutoTeleport", "<gold>{0} will {1} be teleported automatically when they join.", "0 - The player to be teleported automatically", "1 - Whether they had this option toggled (returns: 'no longer', 'now')");
        addDefaultMessage("playersExpelled", "<gray>Pushed away players: <white><em>{0}", "0 - The list of players");
        addDefaultMessage("enchantList", "<dark_gray>All possible enchantments are for this item are: <gray>{0}", "0 - A comma-separated list of enchantment names");
        addDefaultMessage("enchantAddAll", "<gray>Added all possible enchantments for this item.");
        addDefaultMessage("enchantReset", "<gray>Removed every enchantment from this item.");
        addDefaultMessage("enchantMustHoldItem", "<red>You must be holding an enchantable item.");
        addDefaultMessage("enchantSpecify", "<red>Please specify an enchantment.");
        addDefaultMessage("enchantInvalid", "<red>Invalid enchantment for this item.");
        addDefaultMessage("enchantAdd", "<gray>Added {0} {1}", "0 - The enchantment name", "1 - The enchantment level");
        addDefaultMessage("enchantRemove", "<gray>Removed {0}", "0 - The enchantment name");
        addDefaultMessage("enchantInvalidLevel", "<red>Invalid enchantment level.");
    }

    @Override
    public void disable()
    {
        // Unregistering listeners / commands is handled by Plex
    }

    public static Location getRandomLocation(World world)
    {
        double x = ThreadLocalRandom.current().nextDouble(-100000, 100000);
        double z = ThreadLocalRandom.current().nextDouble(-100000, 100000);
        double y = world.getHighestBlockYAt((int)x, (int)z) + 1;
        return new Location(world, x, y, z);
    }

    private Set<Class<?>> getClassesFrom(String packageName)
    {
        Set<Class<?>> classes = new HashSet<>();

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
