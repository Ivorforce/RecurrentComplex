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
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;

/**
 * Created by lukas on 18.06.14.
 */
public class ItemArtifactGenerator extends Item implements GeneratingItem
{
    private Map<ItemStack, List<String>> artifacts;

    protected Map<ItemStack, List<String>> getArtifacts()
    {
        if (artifacts == null)
        {
            artifacts = new HashMap<>();
            addItems(artifacts, Arrays.asList("Sword", "Sabre", "Khopesh", "Xiphos", "Asi", "Makhaira", "Falcata", "Acinaces", "Harpe", "Gladius", "Spatha", "Longsword", "Curtana", "Sabina", "Flamberge", "Two-Hander", "Broadsword", "Schiavona", "Claymore", "Katzbalger", "Rapier", "Smallsword", "Shortsword", "Dirk", "Shotel", "Takoba", "Billao", "Kaskara", "Ida", "Scimitar", "Jian", "Dao", "Nihonto", "Katana", "Saingeom", "Yedo"), Items.DIAMOND_SWORD, Items.GOLDEN_SWORD, Items.IRON_SWORD);
            addItems(artifacts, Arrays.asList("Axe", "Battle Axe", "Broad Axe", "Hatchet", "Ono", "Sagaris", "Parashu"), Items.DIAMOND_AXE, Items.GOLDEN_AXE, Items.IRON_AXE);
            addItems(artifacts, Arrays.asList("Bow"), Items.BOW);
            addItems(artifacts, Arrays.asList("Helmet", "Helm", "Corinthian", "Chalcidian", "Thracian", "Cavalry Helmet", "Bascinet", "Galeo", "Armet", "Great Helm", "Frog-Mouth Helm"), Items.CHAINMAIL_HELMET, Items.DIAMOND_HELMET, Items.GOLDEN_HELMET, Items.IRON_HELMET, Items.LEATHER_HELMET);
            addItems(artifacts, Arrays.asList("Chainmail"), Items.CHAINMAIL_CHESTPLATE);
            addItems(artifacts, Arrays.asList("Armour", "Chestplate"), Items.CHAINMAIL_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.IRON_CHESTPLATE, Items.LEATHER_CHESTPLATE);
            addItems(artifacts, Arrays.asList("Leggings"), Items.CHAINMAIL_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.IRON_LEGGINGS, Items.LEATHER_LEGGINGS);
            addItems(artifacts, Arrays.asList("Boots", "Shoes"), Items.CHAINMAIL_BOOTS, Items.DIAMOND_BOOTS, Items.GOLDEN_BOOTS, Items.IRON_BOOTS, Items.LEATHER_BOOTS);
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
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote)
            return ItemInventoryGenerationTag.applyGeneratorToInventory((WorldServer) worldIn, pos, this, stack) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;

        return super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }


    @Override
    public void generateInInventory(WorldServer server, IInventory inventory, Random random, ItemStack stack, int fromSlot)
    {
        if (!MinecraftForge.EVENT_BUS.post(new ItemGenerationEvent.Artifact(server, inventory, random, stack, fromSlot)))
        {
            Set<ItemStack> stacks = getArtifacts().keySet();
            ItemStack[] stackArray = stacks.toArray(new ItemStack[stacks.size()]);
            ItemStack origStack = stackArray[random.nextInt(stackArray.length)];
            ItemStack artifactStack = origStack.copy();

            int enchantLevel = random.nextInt(20);

            List enchantments = EnchantmentHelper.buildEnchantmentList(random, artifactStack, enchantLevel, false);

            if (enchantments == null)
            {
                enchantments = EnchantmentHelper.buildEnchantmentList(random, new ItemStack(Items.IRON_AXE), enchantLevel, false);
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
