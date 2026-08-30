package dev.plex.extras.listener;

import dev.plex.extras.TFMExtras;
import dev.plex.extras.command.OrbitCommand;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class OrbitEffectListener implements Listener
{
    private final TFMExtras module;

    public OrbitEffectListener(TFMExtras module)
    {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPotionEffectRemove(EntityPotionEffectEvent event)
    {
        if (event.getEntity() instanceof Player player)
        {
            if ((event.getAction() == EntityPotionEffectEvent.Action.CLEARED || event.getAction() == EntityPotionEffectEvent.Action.REMOVED)
                    && event.getModifiedType() == PotionEffectType.LEVITATION)
            {
                module.scheduler().runEntityLater(player, () ->
                {
                    Integer strength = OrbitCommand.orbitStrength(player.getUniqueId());
                    if (strength != null)
                    {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, Integer.MAX_VALUE, strength, false, false));
                    }
                }, 2);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event)
    {
        Player player = event.getPlayer();

        GameMode newGameMode = event.getNewGameMode();
        module.scheduler().runEntityLater(player, () ->
        {
            if (OrbitCommand.orbitStrength(player.getUniqueId()) != null && newGameMode != GameMode.SURVIVAL)
            {
                player.setGameMode(GameMode.SURVIVAL);
            }
        }, 2);
    }
}
