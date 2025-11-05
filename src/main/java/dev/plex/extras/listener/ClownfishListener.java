package dev.plex.extras.listener;

import dev.plex.extras.TFMExtras;
import dev.plex.listener.PlexListener;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ClownfishListener extends PlexListener
{

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();

        if (!player.hasPermission("plex.tfmextras.clownfish"))
        {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item.getType() == Material.TROPICAL_FISH && item.hasItemMeta())
            {
                ItemMeta meta = item.getItemMeta();

                if (meta.hasDisplayName() && Objects.equals(meta.displayName(), Component.text("Clownfish")))
                {
                    final List<String> restrictedPlayers = TFMExtras.getModule().getConfig().getStringList("server.clownfish.restricted");
                    if (restrictedPlayers.contains(player.getUniqueId().toString()))
                    {
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>You have been restricted from using the clownfish"));
                        return;
                    }

                    double radius = TFMExtras.getModule().getConfig().getInt("server.clownfish.radius");
                    double strength = TFMExtras.getModule().getConfig().getInt("server.clownfish.strength");

                    List<String> pushedPlayers = new ArrayList<>();
                    final Vector senderPos = player.getLocation().toVector();
                    final List<Player> players = player.getWorld().getPlayers();
                    final List<String> toggledPlayers = TFMExtras.getModule().getConfig().getStringList("server.clownfish.toggled_players");

                    for (final Player target : players)
                    {
                        if (target.equals(player) || toggledPlayers.contains(target.getUniqueId().toString()))
                        {
                            continue;
                        }

                        final Location targetPos = target.getLocation();
                        final Vector targetPosVec = targetPos.toVector();

                        if (targetPosVec.distanceSquared(senderPos) < (radius * radius))
                        {
                            target.setFlying(false);

                            for (Sound sound : RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT))
                            {
                                if (sound.toString().contains("HIT"))
                                {
                                    target.playSound(target.getLocation(), sound, 100.0f, 0.5f + new Random().nextFloat() * 2.0f);
                                }
                            }

                            target.getWorld().spawnParticle(Particle.CLOUD, target.getLocation(), 5);
                            target.setVelocity(targetPosVec.subtract(senderPos).normalize().multiply(strength));

                            pushedPlayers.add(target.getName());
                        }
                    }

                    if (!pushedPlayers.isEmpty())
                    {
                        for (Sound sound : RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT))
                        {
                            if (sound.toString().contains("HIT"))
                            {
                                player.playSound(player.getLocation(), sound, 100.0f, 0.5f + new Random().nextFloat() * 2.0f);
                            }
                        }
                    }
                }
            }
        }
    }
}
