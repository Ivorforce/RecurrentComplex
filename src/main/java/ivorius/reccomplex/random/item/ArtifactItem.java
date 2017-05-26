/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.random.item;

import ivorius.reccomplex.random.Artifact;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by lukas on 02.10.16.
 */
public class ArtifactItem
{
    private static final Map<ItemStack, List<String>> artifacts = getArtifacts();

    protected static Map<ItemStack, List<String>> getArtifacts()
    {
        Map<ItemStack, List<String>> artifacts = new HashMap<>();

        addItems(artifacts, Arrays.asList("Sword", "Sabre", "Khopesh", "Xiphos", "Asi", "Makhaira", "Falcata", "Acinaces", "Harpe", "Gladius", "Spatha", "Longsword", "Curtana", "Sabina", "Flamberge", "Two-Hander", "Broadsword", "Schiavona", "Claymore", "Katzbalger", "Rapier", "Smallsword", "Shortsword", "Dirk", "Shotel", "Takoba", "Billao", "Kaskara", "Ida", "Scimitar", "Jian", "Dao", "Nihonto", "Katana", "Saingeom", "Yedo"), Items.DIAMOND_SWORD, Items.GOLDEN_SWORD, Items.IRON_SWORD);
        addItems(artifacts, Arrays.asList("Axe", "Battle Axe", "Broad Axe", "Hatchet", "Ono", "Sagaris", "Parashu"), Items.DIAMOND_AXE, Items.GOLDEN_AXE, Items.IRON_AXE);
        addItems(artifacts, Arrays.asList("Bow"), Items.BOW);
        addItems(artifacts, Arrays.asList("Helmet", "Helm", "Corinthian", "Chalcidian", "Thracian", "Cavalry Helmet", "Bascinet", "Galeo", "Armet", "Great Helm", "Frog-Mouth Helm"), Items.CHAINMAIL_HELMET, Items.DIAMOND_HELMET, Items.GOLDEN_HELMET, Items.IRON_HELMET, Items.LEATHER_HELMET);
        addItems(artifacts, Arrays.asList("Chainmail"), Items.CHAINMAIL_CHESTPLATE);
        addItems(artifacts, Arrays.asList("Armour", "Chestplate"), Items.CHAINMAIL_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.IRON_CHESTPLATE, Items.LEATHER_CHESTPLATE);
        addItems(artifacts, Arrays.asList("Leggings"), Items.CHAINMAIL_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.IRON_LEGGINGS, Items.LEATHER_LEGGINGS);
        addItems(artifacts, Arrays.asList("Boots", "Shoes"), Items.CHAINMAIL_BOOTS, Items.DIAMOND_BOOTS, Items.GOLDEN_BOOTS, Items.IRON_BOOTS, Items.LEATHER_BOOTS);

        return artifacts;
    }

    private static void addItems(Map<ItemStack, List<String>> list, List<String> names, Item... items)
    {
        for (Item item : items)
        {
            ItemStack stack = new ItemStack(item);

            if (list.containsKey(stack))
            {
                list.get(stack).addAll(names);
            }
            else
            {
                List<String> used = new ArrayList<>();
                used.addAll(names);
                list.put(stack, used);
            }
        }
    }

    @Nonnull
    public static ItemStack any(Random random)
    {
        Set<ItemStack> stacks = artifacts.keySet();
        ItemStack[] stackArray = stacks.toArray(new ItemStack[stacks.size()]);
        ItemStack origStack = stackArray[random.nextInt(stackArray.length)];
        ItemStack artifactStack = origStack.copy();

        int enchantLevel = random.nextInt(20);

        List enchantments = EnchantmentHelper.buildEnchantmentList(random, artifactStack, enchantLevel, false);

        if (enchantments.isEmpty())
        {
            enchantments = EnchantmentHelper.buildEnchantmentList(random, new ItemStack(Items.IRON_AXE), enchantLevel, false);
        }

        if (!enchantments.isEmpty())
        {
            for (Object enchantment : enchantments)
            {
                EnchantmentData enchantmentdata = (EnchantmentData) enchantment;
                artifactStack.addEnchantment(enchantmentdata.enchantment, enchantmentdata.enchantmentLevel);
            }
        }

        List<String> possibleNames = artifacts.get(origStack);

        Artifact artifact = Artifact.randomArtifact(random, possibleNames.get(random.nextInt(possibleNames.size())));
        artifactStack.setStackDisplayName(artifact.getFullName());
        return artifactStack;
    }
}
