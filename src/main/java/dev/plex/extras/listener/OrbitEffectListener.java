package dev.plex.extras.listener;

import dev.plex.Plex;
import dev.plex.extras.command.OrbitCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import dev.plex.listener.PlexListener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class OrbitEffectListener extends PlexListener
{
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPotionEffectRemove(EntityPotionEffectEvent event)
    {
        if (event.getEntity() instanceof Player player)
        {
            if ((event.getAction() == EntityPotionEffectEvent.Action.CLEARED || event.getAction() == EntityPotionEffectEvent.Action.REMOVED)
                    && event.getModifiedType() == PotionEffectType.LEVITATION)
            {
                Bukkit.getScheduler().runTaskLater(Plex.get(), () ->
                {
                    if (OrbitCommand.isPlayerOrbited(player.getUniqueId()))
                    {
                        Integer strength = OrbitCommand.getOrbitStrength(player.getUniqueId());
                        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, Integer.MAX_VALUE, strength, false, false));
                    }
                }, 2);
            }
        }
    }
}