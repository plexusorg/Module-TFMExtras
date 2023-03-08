package dev.plex.command;

import com.google.common.collect.Lists;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.command.source.RequiredCommandSource;
import dev.plex.rank.enums.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@CommandParameters(name = "enchant", description = "Enchants an item", usage = "/<command> <add | reset | list | addall | remove>", aliases = "enchantment")
@CommandPermissions(level = Rank.OP, permission = "plex.tfmextras.enchant", source = RequiredCommandSource.IN_GAME)
public class EnchantCommand extends PlexCommand
{
    @Override
    protected Component execute(@NotNull CommandSender sender, @Nullable Player player, @NotNull String[] args)
    {
        if (args.length == 0)
        {
            return usage();
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR)
        {
            return MiniMessage.miniMessage().deserialize("<red>You must be holding an enchantable item.");
        }

        if (args.length == 1)
        {
            if (args[0].equalsIgnoreCase("list"))
            {
                return MiniMessage.miniMessage().deserialize("<dark_gray>All possible enchantments are for this item are: <gray>" + StringUtils.join(getEnchantmentNames(item), ", "));
            }
            if (args[0].equalsIgnoreCase("addall"))
            {
                getEnchantments(item).forEach(enchantment -> item.addEnchantment(enchantment, enchantment.getMaxLevel()));
                player.playSound(player, Sound.BLOCK_ANVIL_USE, 1, 1);
                return MiniMessage.miniMessage().deserialize("<gray>Added all possible enchantments for this item.");
            }
            if (args[0].equalsIgnoreCase("reset"))
            {
                item.getEnchantments().keySet().forEach(item::removeEnchantment);
                player.playSound(player, Sound.BLOCK_ANVIL_USE, 1, 1);
                return MiniMessage.miniMessage().deserialize("<gray>Removed every enchantment from this item.");
            }
        }
        return null;
    }

    private List<Enchantment> getEnchantments(ItemStack item)
    {
        List<Enchantment> enchants = Lists.newArrayList();
        Arrays.stream(Enchantment.values()).filter(enchantment -> enchantment.canEnchantItem(item)).forEach(enchants::add);
        return enchants;
    }

    private String[] getEnchantmentNames(ItemStack item)
    {
        return getEnchantments(item).stream().map(enchantment -> enchantment.key().value()).toArray(String[]::new);
    }
}
