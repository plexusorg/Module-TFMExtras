package dev.plex.extras.command;

import com.google.common.collect.Lists;
import dev.plex.command.PlexCommand;
import dev.plex.command.annotation.CommandParameters;
import dev.plex.command.annotation.CommandPermissions;
import dev.plex.command.source.RequiredCommandSource;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandParameters(name = "enchant", description = "Enchants an item", usage = "/<command> <add | reset | list | addall | remove>", aliases = "enchantment")
@CommandPermissions(permission = "plex.tfmextras.enchant", source = RequiredCommandSource.IN_GAME)
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
            return messageComponent("enchantMustHoldItem");
        }

        switch (args[0].toLowerCase())
        {
            case "add":
                if (args.length < 2)
                {
                    return messageComponent("enchantSpecify");
                }

                Enchantment enchantmentToAdd = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(args[1].toLowerCase()));
                if (enchantmentToAdd == null || !enchantmentToAdd.canEnchantItem(item))
                {
                    return messageComponent("enchantInvalid");
                }

                int levelToAdd = enchantmentToAdd.getMaxLevel();
                if (args.length >= 3)
                {
                    try
                    {
                        levelToAdd = Integer.parseInt(args[2]);
                        if (levelToAdd < 1 || levelToAdd > 255)
                        {
                            return messageComponent("enchantInvalidLevel");
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        return messageComponent("enchantInvalidLevel");
                    }
                }

                item.addUnsafeEnchantment(enchantmentToAdd, levelToAdd);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
                return messageComponent("enchantAdd", enchantmentToAdd.getKey().getKey(), levelToAdd);

            case "remove":
                if (args.length < 2)
                {
                    return messageComponent("enchantSpecify");
                }

                Enchantment enchantmentToRemove = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(NamespacedKey.minecraft(args[1].toLowerCase()));
                if (enchantmentToRemove == null || !item.containsEnchantment(enchantmentToRemove))
                {
                    return messageComponent("enchantInvalid");
                }

                item.removeEnchantment(enchantmentToRemove);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
                return messageComponent("enchantRemove", enchantmentToRemove.getKey().getKey());

            case "list":
                return messageComponent("enchantList", StringUtils.join(getEnchantmentNames(item), ", "));

            case "addall":
                getEnchantments(item).forEach(enchantment -> item.addEnchantment(enchantment, enchantment.getMaxLevel()));
                player.playSound(player, Sound.BLOCK_ANVIL_USE, 1, 1);
                return messageComponent("enchantAddAll");

            case "reset":
                item.getEnchantments().keySet().forEach(item::removeEnchantment);
                player.playSound(player, Sound.BLOCK_ANVIL_USE, 1, 1);
                return messageComponent("enchantReset");
        }
        return null;
    }

    private List<Enchantment> getEnchantments(ItemStack item)
    {
        List<Enchantment> enchants = Lists.newArrayList();
        RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream().filter(enchantment -> enchantment.canEnchantItem(item)).forEach(enchants::add);
        return enchants;
    }

    private String[] getEnchantmentNames(ItemStack item)
    {
        return getEnchantments(item).stream().map(enchantment -> enchantment.key().value()).toArray(String[]::new);
    }

    @Override
    public @NotNull List<String> smartTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException
    {
        if (silentCheckPermission(sender, this.getPermission()))
        {
            if (args.length == 1)
            {
                return Arrays.asList("add", "reset", "list", "addall", "remove");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))
            {
                Player player = Bukkit.getPlayer(sender.getName());
                if (player != null)
                {
                    return List.of(getEnchantmentNames(player.getInventory().getItemInMainHand()));
                }
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
