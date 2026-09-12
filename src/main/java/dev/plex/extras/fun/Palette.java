package dev.plex.extras.fun;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;

public final class Palette
{
    public static final List<Material> RAINBOW = List.of(
            Material.RED_CONCRETE,
            Material.ORANGE_CONCRETE,
            Material.YELLOW_CONCRETE,
            Material.LIME_CONCRETE,
            Material.GREEN_CONCRETE,
            Material.CYAN_CONCRETE,
            Material.LIGHT_BLUE_CONCRETE,
            Material.BLUE_CONCRETE,
            Material.PURPLE_CONCRETE,
            Material.MAGENTA_CONCRETE,
            Material.PINK_CONCRETE,
            Material.WHITE_CONCRETE,
            Material.LIGHT_GRAY_CONCRETE,
            Material.GRAY_CONCRETE,
            Material.BLACK_CONCRETE,
            Material.BROWN_CONCRETE);

    private Palette()
    {
    }

    public static Material random()
    {
        return RAINBOW.get(ThreadLocalRandom.current().nextInt(RAINBOW.size()));
    }

    public static Material at(int index)
    {
        return RAINBOW.get(Math.floorMod(index, RAINBOW.size()));
    }

    public static Material byName(String dyeName)
    {
        if (dyeName == null)
        {
            return null;
        }
        return Material.matchMaterial(dyeName.trim().toUpperCase(Locale.ROOT) + "_CONCRETE");
    }
}
