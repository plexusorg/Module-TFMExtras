package dev.plex.extras;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import dev.plex.api.PlexApi;
import dev.plex.config.ModuleConfig;
import dev.plex.extras.command.AdminInfoCommand;
import dev.plex.extras.command.AutoClearCommand;
import dev.plex.extras.command.AutoTeleportCommand;
import dev.plex.extras.command.CakeCommand;
import dev.plex.extras.command.CartSitCommand;
import dev.plex.extras.command.ClearChatCommand;
import dev.plex.extras.command.ClownfishCommand;
import dev.plex.extras.command.CloudClearCommand;
import dev.plex.extras.command.EjectCommand;
import dev.plex.extras.command.EnchantCommand;
import dev.plex.extras.command.EnglishMfCommand;
import dev.plex.extras.command.ExpelCommand;
import dev.plex.extras.command.JumpPadsCommand;
import dev.plex.extras.command.OrbitCommand;
import dev.plex.extras.command.RandomFishCommand;
import dev.plex.extras.jumppads.JumpPads;
import dev.plex.listener.PlexListener;
import dev.plex.module.PlexModule;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

    public static PlexApi plexApi()
    {
        return module.api();
    }

    @Override
    public void load()
    {
        module = this;
        config = new ModuleConfig(this, "tfmextras/config.yml", "config.yml");
        config.load();
        loadMessages("tfmextras/messages.yml");
        jumpPads = new JumpPads();
    }

    @Override
    public void enable()
    {
        List.of(
                new AdminInfoCommand(),
                new AutoClearCommand(),
                new AutoTeleportCommand(),
                new CakeCommand(),
                new CartSitCommand(),
                new ClearChatCommand(),
                new ClownfishCommand(),
                new CloudClearCommand(),
                new EjectCommand(),
                new EnchantCommand(),
                new EnglishMfCommand(),
                new ExpelCommand(),
                new JumpPadsCommand(),
                new OrbitCommand(),
                new RandomFishCommand()
        ).forEach(this::registerCommand);

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
                    plexApi().logging().error("Unable to find class {0} in {1}", info.getName(), packageName);
                }

            });
        }
        catch (IOException var4)
        {
            plexApi().logging().error("Something went wrong while fetching classes from {0}", packageName);
            throw new RuntimeException(var4);
        }

        return Collections.unmodifiableSet(classes);
    }
}
