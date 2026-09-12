package dev.plex.extras.fun;

import dev.plex.extras.TFMExtras;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public final class Paintball implements Listener
{
    private static final String RANDOM = "random";
    private static final int RADIUS = 2;

    private final TFMExtras module;
    private final TemporaryBlocks blocks;
    private final NamespacedKey marker;

    public Paintball(TFMExtras module, TemporaryBlocks blocks)
    {
        this.module = module;
        this.blocks = blocks;
        this.marker = new NamespacedKey(module.plugin(), "paintball");
    }

    public ItemStack item(@Nullable String color)
    {
        ItemStack paintball = new ItemStack(Material.SNOWBALL, 16);
        ItemMeta meta = paintball.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize("<!italic><rainbow>Paintball"));
        meta.getPersistentDataContainer().set(marker, PersistentDataType.STRING, color == null ? RANDOM : color);
        paintball.setItemMeta(meta);
        return paintball;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event)
    {
        if (!(event.getEntity() instanceof Snowball snowball))
        {
            return;
        }

        ItemStack thrown = snowball.getItem();
        if (!thrown.hasItemMeta())
        {
            return;
        }
        String color = thrown.getItemMeta().getPersistentDataContainer().get(marker, PersistentDataType.STRING);
        if (color == null)
        {
            return;
        }

        Block center = impactBlock(event);
        if (center == null)
        {
            return;
        }

        // A creative player can forge the marker, so the stored colour is untrusted until resolved.
        Material material = RANDOM.equals(color) ? Palette.random() : Palette.byName(color);
        if (material == null)
        {
            return;
        }

        BlockData paint = material.createBlockData();
        Map<Block, BlockData> splat = new HashMap<>();
        for (int x = -RADIUS; x <= RADIUS; x++)
        {
            for (int y = -RADIUS; y <= RADIUS; y++)
            {
                for (int z = -RADIUS; z <= RADIUS; z++)
                {
                    if (x * x + y * y + z * z > RADIUS * RADIUS)
                    {
                        continue;
                    }
                    Block block = center.getRelative(x, y, z);
                    if (block.getType().isSolid())
                    {
                        splat.put(block, paint);
                    }
                }
            }
        }

        // The projectile hit runs on the region owning the projectile, and therefore the blocks it hit.
        Location impact = center.getLocation().add(0.5, 0.5, 0.5);
        blocks.place(impact, splat, module.getConfig().getInt("server.fun.paintball_fade_ticks", 200));

        World world = impact.getWorld();
        world.spawnParticle(Particle.ITEM_SNOWBALL, impact, 20);
        world.playSound(impact, Sound.ENTITY_SLIME_SQUISH, 1.0f, 1.0f);
    }

    private @Nullable Block impactBlock(ProjectileHitEvent event)
    {
        Block hitBlock = event.getHitBlock();
        if (hitBlock != null)
        {
            BlockFace face = event.getHitBlockFace();
            return face == null ? hitBlock : hitBlock.getRelative(face);
        }

        Entity hitEntity = event.getHitEntity();
        return hitEntity == null ? null : hitEntity.getLocation().getBlock();
    }
}
