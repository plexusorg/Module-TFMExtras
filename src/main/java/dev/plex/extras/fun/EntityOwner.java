package dev.plex.extras.fun;

import dev.plex.module.PlexModule;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

final class EntityOwner
{
    private EntityOwner()
    {
    }

    // A scheduler hop is only for a real ownership crossing; a caller on the entity's own region
    // already owns the entity.
    static void run(PlexModule module, Entity entity, Runnable action, Runnable retired)
    {
        if (Bukkit.isOwnedByCurrentRegion(entity))
        {
            action.run();
            return;
        }

        ScheduledTask task = module.ownTask(entity.getScheduler().run(module.plugin(),
                ignored -> action.run(), retired));
        if (task == null)
        {
            retired.run();
        }
    }
}
