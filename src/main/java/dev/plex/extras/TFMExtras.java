package dev.plex.extras;

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
import dev.plex.extras.listener.ClownfishListener;
import dev.plex.extras.listener.JumpPadsListener;
import dev.plex.extras.listener.OrbitEffectListener;
import dev.plex.extras.listener.PlayerListener;
import dev.plex.module.PlexModule;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;

public class TFMExtras extends PlexModule
{
    @Getter
    private JumpPads jumpPads;

    @Getter
    private ModuleConfig config;

    @Override
    public void load()
    {
        config = new ModuleConfig(this, "config.yml", "config.yml");
        config.load();
        loadMessages("messages.yml");
        jumpPads = new JumpPads(config.getInt("server.jumppad_strength", 1));
        List.of(
                new AdminInfoCommand(this),
                new AutoClearCommand(this),
                new AutoTeleportCommand(this),
                new CakeCommand(),
                new CartSitCommand(),
                new ClearChatCommand(),
                new ClownfishCommand(this),
                new CloudClearCommand(),
                new EjectCommand(),
                new EnchantCommand(),
                new EnglishMfCommand(),
                new ExpelCommand(),
                new JumpPadsCommand(this),
                new OrbitCommand(),
                new RandomFishCommand()
        ).forEach(this::registerCommand);
    }

    @Override
    public void enable()
    {
        List.of(
                new ClownfishListener(this),
                new JumpPadsListener(this),
                new OrbitEffectListener(this),
                new PlayerListener(this)
        ).forEach(this::registerListener);
    }

    @Override
    public void disable()
    {
        // Unregistering listeners / commands is handled by Plex
    }

    public Location getRandomLocation(World world)
    {
        double x = ThreadLocalRandom.current().nextDouble(-100000, 100000);
        double z = ThreadLocalRandom.current().nextDouble(-100000, 100000);
        double y = world.getHighestBlockYAt((int)x, (int)z) + 1;
        return new Location(world, x, y, z);
    }
}
