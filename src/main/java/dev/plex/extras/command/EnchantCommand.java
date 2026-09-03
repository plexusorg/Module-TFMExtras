package dev.plex.extras.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.plex.command.SimplePlexCommand;
import dev.plex.command.source.RequiredCommandSource;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnchantCommand extends SimplePlexCommand
{
    public EnchantCommand()
    {
        super(command("enchant")
                .description("Enchants an item")
                .usage("/<command> <add | reset | list | addall | remove>")
                .aliases("enchantment")
                .permission("plex.tfmextras.enchant")
                .source(RequiredCommandSource.IN_GAME)
                .build());
    }
    @Override
    protected void configureCommand(LiteralArgumentBuilder<CommandSourceStack> command)
    {
        command.executes(context -> executeCommand(context, (sender, player) -> usage()));
        command.then(word("action").suggests((context, builder) -> suggestMatching(builder, List.of("add", "reset", "list", "addall", "remove")))
                .executes(context -> executeCommand(context, (sender, player) -> executeTyped(player, string(context, "action"), null, null)))
                .then(word("enchantment").suggests((context, builder) ->
                {
                    String action = string(context, "action");
                    if (action.equalsIgnoreCase("add") || action.equalsIgnoreCase("remove"))
                    {
                        return suggestMatching(builder, RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream()
                                .map(enchantment -> enchantment.key().value()).toList());
                    }
                    return builder.buildFuture();
                })
                        .executes(context -> executeCommand(context, (sender, player) -> executeTyped(player, string(context, "action"), string(context, "enchantment"), null)))
                        .then(word("level").executes(context -> executeCommand(context, (sender, player) -> executeTyped(player, string(context, "action"), string(context, "enchantment"), string(context, "level"))))
                                .then(greedyString("ignored").executes(context -> executeCommand(context, (sender, player) -> executeTyped(player, string(context, "action"), string(context, "enchantment"), string(context, "level"))))))));
    }

    private Component executeTyped(Player player, String action, @Nullable String enchantmentName, @Nullable String level)
    {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR)
        {
            return messageComponent("enchantMustHoldItem");
        }

        switch (action.toLowerCase())
        {
            case "add":
                if (enchantmentName == null)
                {
                    return messageComponent("enchantSpecify");
                }

                Enchantment enchantmentToAdd = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(NamespacedKey.minecraft(enchantmentName.toLowerCase()));
                if (enchantmentToAdd == null || !enchantmentToAdd.canEnchantItem(item))
                {
                    return messageComponent("enchantInvalid");
                }

                int levelToAdd = enchantmentToAdd.getMaxLevel();
                if (level != null)
                {
                    try
                    {
                        levelToAdd = Integer.parseInt(level);
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
                if (enchantmentName == null)
                {
                    return messageComponent("enchantSpecify");
                }

                Enchantment enchantmentToRemove = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(NamespacedKey.minecraft(enchantmentName.toLowerCase()));
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

}
