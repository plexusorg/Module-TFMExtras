package dev.plex.extras.command;

import org.bukkit.Bukkit;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import dev.plex.extras.TFMExtras;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static dev.plex.api.message.MessagePlaceholder.placeholder;

public class OrbitCommand extends SimplePlexCommand
{
    private final TFMExtras module;

    public OrbitCommand(TFMExtras module)
    {
        super(command("orbit")
                .description("Accelerates the player at a super fast rate")
                .usage("/<command> <target> [<<power> | stop>]")
                .permission("plex.tfmextras.orbit")
                .build());
        this.module = module;
    }

    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> usage()));
        command.then(word("target").suggests((context, builder) -> suggestMatching(builder, onlinePlayerNames()))
                .executes(context -> executeCommand(context,
                        (sender, player) -> executeTyped(sender, string(context, "target"), null)))
                .then(word("power").suggests((context, builder) -> suggestMatching(builder, List.of("stop")))
                        .executes(context -> executeCommand(context, (sender, player) -> executeTyped(sender,
                                string(context, "target"), string(context, "power"))))
                        .then(greedyString("ignored").executes(context -> executeCommand(context,
                                (sender, player) -> executeTyped(sender, string(context, "target"), string(context, "power")))))));
    }

    private Component executeTyped(CommandSender sender, String target, @Nullable String power)
    {
        Player targetPlayer = getNonNullPlayer(target);

        int strength = 100;

        if (power != null)
        {
            if (power.equalsIgnoreCase("stop"))
            {
                stopOrbiting(targetPlayer);
                return messageComponent("stoppedOrbiting", placeholder("player", targetPlayer.getName()));
            }

            try
            {
                strength = Math.max(1, Math.min(150, Integer.parseInt(power)));
            }
            catch (NumberFormatException ex)
            {
                return null;
            }
        }

        if (module.orbitStrength(targetPlayer.getUniqueId()) != null)
        {
            return messageComponent("alreadyOrbited", placeholder("player", targetPlayer.getName()));
        }

        module.startOrbit(targetPlayer, strength);
        broadcast(messageComponent("playerOrbited", placeholder("sender", sender.getName()), placeholder("player", targetPlayer.getName())));
        return null;
    }

    private void stopOrbiting(Player player)
    {
        module.clearOrbitStrength(player.getUniqueId());
        ownTask(player.getScheduler().run(taskOwner(),
                ignored -> player.removePotionEffect(PotionEffectType.LEVITATION), null));
    }
}
