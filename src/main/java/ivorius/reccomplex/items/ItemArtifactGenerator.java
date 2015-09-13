/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import ivorius.reccomplex.events.ItemGenerationEvent;
import ivorius.reccomplex.random.Artifact;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;

/**
 * Created by lukas on 18.06.14.
 */
public class ItemArtifactGenerator extends Item implements InventoryScript
{
    private Map<ItemStack, List<String>> artifacts;

    protected Map<ItemStack, List<String>> getArtifacts()
    {
        if (artifacts == null)
        {
            artifacts = new HashMap<>();
            addItems(artifacts, Arrays.asList("Sword", "Sabre", "Khopesh", "Xiphos", "Asi", "Makhaira", "Falcata", "Acinaces", "Harpe", "Gladius", "Spatha", "Longsword", "Curtana", "Sabina", "Flamberge", "Two-Hander", "Broadsword", "Schiavona", "Claymore", "Katzbalger", "Rapier", "Smallsword", "Shortsword", "Dirk", "Shotel", "Takoba", "Billao", "Kaskara", "Ida", "Scimitar", "Jian", "Dao", "Nihonto", "Katana", "Saingeom", "Yedo"), Items.diamond_sword, Items.golden_sword, Items.iron_sword);
            addItems(artifacts, Arrays.asList("Axe", "Battle Axe", "Broad Axe", "Hatchet", "Ono", "Sagaris", "Parashu"), Items.diamond_axe, Items.golden_axe, Items.iron_axe);
            addItems(artifacts, Arrays.asList("Bow"), Items.bow);
            addItems(artifacts, Arrays.asList("Helmet", "Helm", "Corinthian", "Chalcidian", "Thracian", "Cavalry Helmet", "Bascinet", "Galeo", "Armet", "Great Helm", "Frog-Mouth Helm"), Items.chainmail_helmet, Items.diamond_helmet, Items.golden_helmet, Items.iron_helmet, Items.leather_helmet);
            addItems(artifacts, Arrays.asList("Chainmail"), Items.chainmail_chestplate);
            addItems(artifacts, Arrays.asList("Armour", "Chestplate"), Items.chainmail_chestplate, Items.diamond_chestplate, Items.golden_chestplate, Items.iron_chestplate, Items.leather_chestplate);
            addItems(artifacts, Arrays.asList("Leggings"), Items.chainmail_leggings, Items.diamond_leggings, Items.golden_leggings, Items.iron_leggings, Items.leather_leggings);
            addItems(artifacts, Arrays.asList("Boots", "Shoes"), Items.chainmail_boots, Items.diamond_boots, Items.golden_boots, Items.iron_boots, Items.leather_boots);
        }

        return artifacts;
    }

    private void addItems(Map<ItemStack, List<String>> list, List<String> names, Item... items)
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

    @Override
    public boolean onItemUse(ItemStack usedItem, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
    {
        return ItemInventoryGenerationTag.applyGeneratorToInventory(world, x, y, z, this, usedItem);
    }

    @Override
    public void generateInInventory(IInventory inventory, Random random, ItemStack stack, int fromSlot)
    {
        if (!MinecraftForge.EVENT_BUS.post(new ItemGenerationEvent.Artifact(inventory, random, stack, fromSlot)))
        {
            Set<ItemStack> stacks = getArtifacts().keySet();
            ItemStack[] stackArray = stacks.toArray(new ItemStack[stacks.size()]);
            ItemStack origStack = stackArray[random.nextInt(stackArray.length)];
            ItemStack artifactStack = origStack.copy();

            int enchantLevel = random.nextInt(20);

            List enchantments = EnchantmentHelper.buildEnchantmentList(random, artifactStack, enchantLevel);

            if (enchantments == null)
            {
                enchantments = EnchantmentHelper.buildEnchantmentList(random, new ItemStack(Items.iron_axe), enchantLevel);
            }

            if (enchantments != null)
            {
                for (Object enchantment : enchantments)
                {
                    EnchantmentData enchantmentdata = (EnchantmentData) enchantment;
                    artifactStack.addEnchantment(enchantmentdata.enchantmentobj, enchantmentdata.enchantmentLevel);
                }
            }

            List<String> possibleNames = getArtifacts().get(origStack);

            Artifact artifact = Artifact.randomArtifact(random, possibleNames.get(random.nextInt(possibleNames.size())));
            artifactStack.setStackDisplayName(artifact.getFullName());

            inventory.setInventorySlotContents(fromSlot, artifactStack);
        }
    }
}
