package dev.plex.extras.fun;

import dev.plex.module.PlexModule;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

// Attribute base values persist in player data, so this owner records the value a command
// replaced and restores exactly that value.
public class SessionAttributes implements Listener
{
    private final PlexModule module;
    private final Map<UUID, Map<Attribute, Double>> originals = new ConcurrentHashMap<>();

    public SessionAttributes(PlexModule module)
    {
        this.module = module;
    }

    public void set(Player target, Attribute attribute, double value)
    {
        AttributeInstance instance = target.getAttribute(attribute);
        originals.computeIfAbsent(target.getUniqueId(), ignored -> new ConcurrentHashMap<>())
                .putIfAbsent(attribute, instance.getBaseValue());
        instance.setBaseValue(value);
    }

    public void reset(Player target, Attribute attribute)
    {
        UUID playerId = target.getUniqueId();
        Map<Attribute, Double> recorded = originals.get(playerId);
        if (recorded == null)
        {
            return;
        }

        Double original = recorded.remove(attribute);
        if (original != null)
        {
            target.getAttribute(attribute).setBaseValue(original);
        }
        if (recorded.isEmpty())
        {
            originals.remove(playerId, recorded);
        }
    }

    public void resetAll(Player target)
    {
        Map<Attribute, Double> recorded = originals.remove(target.getUniqueId());
        if (recorded == null)
        {
            return;
        }
        recorded.forEach((attribute, original) -> target.getAttribute(attribute).setBaseValue(original));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        if (originals.containsKey(event.getPlayer().getUniqueId()))
        {
            resetAll(event.getPlayer());
        }
    }

    // Runs during module disable. Plex cancels owned tasks right afterwards, so a player another
    // region owns gets an unowned reset on the Plex plugin instead of a module task.
    public CompletableFuture<Void> resetOnline()
    {
        List<CompletableFuture<Void>> completions = new ArrayList<>();
        for (UUID playerId : originals.keySet())
        {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null)
            {
                continue;
            }
            if (Bukkit.isOwnedByCurrentRegion(player))
            {
                resetAll(player);
                continue;
            }

            CompletableFuture<Void> completion = new CompletableFuture<>();
            completions.add(completion);
            boolean scheduled = player.getScheduler().execute(module.plugin(), () ->
            {
                resetAll(player);
                completion.complete(null);
            }, () -> completion.complete(null), 0L);
            if (!scheduled)
            {
                completion.complete(null);
            }
        }
        return CompletableFuture.allOf(completions.toArray(CompletableFuture[]::new));
    }
}
