package dev.plex.extras.hook;

import com.google.common.collect.Sets;
import com.infernalsuite.aswm.api.SlimePlugin;
import com.infernalsuite.aswm.api.exceptions.CorruptedWorldException;
import com.infernalsuite.aswm.api.exceptions.NewerFormatException;
import com.infernalsuite.aswm.api.exceptions.UnknownWorldException;
import com.infernalsuite.aswm.api.exceptions.WorldAlreadyExistsException;
import com.infernalsuite.aswm.api.exceptions.WorldLockedException;
import com.infernalsuite.aswm.api.loaders.SlimeLoader;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.infernalsuite.aswm.api.world.properties.SlimeProperties;
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap;
import dev.plex.extras.TFMExtras;
import dev.plex.util.PlexLog;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * @author Taah
 * @since 2:19 PM [23-08-2023]
 */
public class SlimeWorldHook implements IHook<SlimePlugin>
{
    private static final String WORLD_NOT_FOUND = "<red>This world could not be found!";
    private static final String STORAGE_FAILURE = "<red>This world cannot be stored!";

    private final Set<String> loadedWorlds = Sets.newHashSet();

//    private final List<>

    @Getter
    private SlimeLoader loader;


    @Override
    public void onEnable(TFMExtras module)
    {
        if (plugin() == null)
        {
            PlexLog.error("Cannot find SlimeWorldManager plugin");
            return;
        }

        PlexLog.log("<green>Enabling SWM Hook");

        this.loader = plugin().getLoader("mysql");
    }

    @Override
    public void onDisable(TFMExtras module)
    {
        PlexLog.log("<green>Disabling SWM Hook");
        AtomicInteger i = new AtomicInteger();
        loadedWorlds.forEach(s ->
        {
            final World world = Bukkit.getWorld(s);
            if (world != null)
            {
                world.save();
                i.getAndIncrement();
            }
        });
        PlexLog.log("<green>SWM Hook saved " + i.get() + " worlds");
    }

    public void loadAllWorlds()
    {
        try
        {
            this.loader.listWorlds().forEach(s ->
            {
                final SlimePropertyMap slimePropertyMap = new SlimePropertyMap();
                slimePropertyMap.setValue(SlimeProperties.PVP, false);

                try
                {
                    SlimeWorld world = this.plugin().loadWorld(this.loader, s, false, slimePropertyMap);
                    this.plugin().loadWorld(world);
                    this.loader.unlockWorld(s);
                }
                catch (UnknownWorldException | WorldLockedException | CorruptedWorldException | NewerFormatException |
                       IllegalArgumentException ex)
                {
                    PlexLog.error(ex.getMessage());
                }
                catch (IOException e)
                {
                    PlexLog.error(STORAGE_FAILURE);
                    return;
                }

                final World world = Bukkit.getWorld(s);
                if (world == null)
                {
                    PlexLog.error(WORLD_NOT_FOUND);
                    return;
                }
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                world.setGameRule(GameRule.DISABLE_RAIDS, true);
                world.setGameRule(GameRule.DO_INSOMNIA, false);
                world.setGameRule(GameRule.DO_FIRE_TICK, false);
                world.setSpawnLocation(0, 130, 0);
                world.setAutoSave(true);

                loadedWorlds.add(s);

                double configuratedSize = TFMExtras.getModule().getConfig().getDouble("player-worlds.size");
                world.getWorldBorder().setCenter(world.getSpawnLocation());
                world.getWorldBorder().setSize(configuratedSize == 0 ? 500 : configuratedSize);
                world.getWorldBorder().setDamageAmount(0);
                world.getWorldBorder().setDamageBuffer(0);
                PlexLog.debug("Loaded {0}", s);
            });
        }
        catch (IOException | IllegalArgumentException ex)
        {
            PlexLog.error(ex.getMessage());
        }
    }

    public boolean isWorldLoaded(String world)
    {
        return loadedWorlds.stream().anyMatch(s -> s.equals(world));
    }

    public void deleteWorld(String world)
    {
        try
        {
            if (Bukkit.getWorld(world) != null)
            {
                Bukkit.unloadWorld(world, false);
            }
            this.loader.deleteWorld(world);
        }
        catch (UnknownWorldException | IOException e)
        {
            PlexLog.error(e.getMessage());
        }
    }

    public Pair<World, Boolean> createPlayerWorld(UUID uuid)
    {
        final SlimePropertyMap slimePropertyMap = new SlimePropertyMap();
        slimePropertyMap.setValue(SlimeProperties.PVP, false);

        boolean newWorld = false;
        try
        {
            slimePropertyMap.setValue(SlimeProperties.SPAWN_X, 0);
            slimePropertyMap.setValue(SlimeProperties.SPAWN_Y, 130);
            slimePropertyMap.setValue(SlimeProperties.SPAWN_Z, 0);
            final SlimeWorld slimeWorld = this.plugin().createEmptyWorld(this.loader, uuid.toString(), false, slimePropertyMap);
            this.plugin().loadWorld(slimeWorld);
            newWorld = true;
        }
        catch (WorldAlreadyExistsException e)
        {
            try
            {
                SlimeWorld world = this.plugin().loadWorld(this.loader, uuid.toString(), false, slimePropertyMap);
                this.plugin().loadWorld(world);
                this.loader.unlockWorld(uuid.toString());
            }
            catch (WorldLockedException | CorruptedWorldException | NewerFormatException | UnknownWorldException |
                   IOException | IllegalArgumentException ex)
            {
                PlexLog.error(ex.getMessage());
            }

        }
        catch (IOException e)
        {
            PlexLog.error(STORAGE_FAILURE);
        }
        catch (WorldLockedException | UnknownWorldException e)
        {
            throw new RuntimeException(e);
        }

        final World world = Bukkit.getWorld(uuid.toString());
        if (world == null)
        {
            PlexLog.error(WORLD_NOT_FOUND);
            return null;
        }
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.setGameRule(GameRule.DO_INSOMNIA, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setSpawnLocation(0, 130, 0);
        world.setAutoSave(true);

        if (newWorld)
        {
            world.getBlockAt(0, 128, 0).setType(Material.STONE);
        }

        loadedWorlds.add(uuid.toString());

        double configuratedSize = TFMExtras.getModule().getConfig().getDouble("player-worlds.size");
        world.getWorldBorder().setCenter(world.getSpawnLocation());
        world.getWorldBorder().setSize(configuratedSize == 0 ? 500 : configuratedSize);
        world.getWorldBorder().setDamageAmount(0);
        world.getWorldBorder().setDamageBuffer(0);

        final WorldLoadEvent event = new WorldLoadEvent(world);
        Bukkit.getServer().getPluginManager().callEvent(event);

        return Pair.of(world, newWorld);
    }

    public Set<String> loadedWorlds()
    {
        return this.loadedWorlds;
    }

    @Override
    public SlimePlugin plugin()
    {
        return (SlimePlugin)Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
    }
}
