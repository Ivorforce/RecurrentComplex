/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.item;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.RCGuiHandler;
import ivorius.reccomplex.world.storage.loot.GenericItemCollection.Component;
import ivorius.reccomplex.world.storage.loot.GenericItemCollectionRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 05.01.15.
 */
public class ItemInventoryGenComponentTag extends Item implements GeneratingItem
{
    public static String componentKey(ItemStack stack)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("itemCollectionKey", Constants.NBT.TAG_STRING))
            return stack.getTagCompound().getString("itemCollectionKey");
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("display", Constants.NBT.TAG_COMPOUND)) // Legacy - Display Name
        {
            NBTTagCompound nbttagcompound = stack.getTagCompound().getCompoundTag("display");
            if (nbttagcompound.hasKey("Name", Constants.NBT.TAG_STRING))
                return nbttagcompound.getString("Name");
        }


        return null;
    }

    public static void setComponentKey(ItemStack stack, String generatorKey)
    {
        stack.setTagInfo("itemCollectionKey", new NBTTagString(generatorKey));
    }

    public static Component component(ItemStack stack)
    {
        return GenericItemCollectionRegistry.INSTANCE.get(componentKey(stack));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote)
            RCGuiHandler.editInventoryGenComponent(playerIn, componentKey(itemStackIn), component(itemStackIn), null);

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void generateInInventory(WorldServer server, IInventory inventory, Random random, ItemStack stack, int fromSlot)
    {
        Component component = component(stack);

        if (component != null)
            inventory.setInventorySlotContents(fromSlot, component.getRandomItemStack(random));
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public String getItemStackDisplayName(ItemStack stack)
    {
        String key = componentKey(stack);

        return key != null ? key : super.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedInformation)
    {
        super.addInformation(stack, player, list, advancedInformation);

        Component component = component(stack);

        if (component != null)
        {
            list.add(component.inventoryGeneratorID);
            list.add(GenericItemCollectionRegistry.INSTANCE.hasActive(componentKey(stack))
                    ? IvTranslations.format("inventoryGen.active", TextFormatting.GREEN, TextFormatting.RESET)
                    : IvTranslations.format("inventoryGen.inactive", TextFormatting.RED, TextFormatting.RESET));
        }
        else
            list.add(IvTranslations.get("inventoryGen.create"));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void getSubItems(Item item, CreativeTabs creativeTabs, NonNullList<ItemStack> list)
    {
        super.getSubItems(item, creativeTabs, list);

        GenericItemCollectionRegistry.INSTANCE.ids().stream().sorted().forEach(key ->
        {
            ItemStack stack = new ItemStack(item);
            setComponentKey(stack, key);
            list.add(stack);
        });
    }
}
