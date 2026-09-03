package dev.plex.extras.listener;

import dev.plex.extras.TFMExtras;
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
    private static final int ORBIT_EFFECT_TICKS = 20 * 10;
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
                module.ownTask(player.getScheduler().runDelayed(module.plugin(), task ->
                {
                    Integer strength = module.orbitStrength(player.getUniqueId());
                    if (strength != null)
                    {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, ORBIT_EFFECT_TICKS, strength, false, false));
                    }
                }, null, 2));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event)
    {
        Player player = event.getPlayer();

        GameMode newGameMode = event.getNewGameMode();
        module.ownTask(player.getScheduler().runDelayed(module.plugin(), task ->
        {
            if (module.orbitStrength(player.getUniqueId()) != null && newGameMode != GameMode.SURVIVAL)
            {
                player.setGameMode(GameMode.SURVIVAL);
            }
        }, null, 2));
    }
}
