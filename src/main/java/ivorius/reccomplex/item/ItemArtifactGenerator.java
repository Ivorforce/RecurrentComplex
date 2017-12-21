/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.item;

import ivorius.reccomplex.events.ItemGenerationEvent;
import ivorius.reccomplex.random.item.ArtifactItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.*;

/**
 * Created by lukas on 18.06.14.
 */
public class ItemArtifactGenerator extends Item implements GeneratingItem
{
    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (!worldIn.isRemote)
            return ItemInventoryGenerationTag.applyGeneratorToInventory((WorldServer) worldIn, pos, this, stack) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;

        return EnumActionResult.SUCCESS;
    }

    @Override
    public void generateInInventory(WorldServer server, IItemHandlerModifiable inventory, Random random, ItemStack stack, int fromSlot)
    {
        if (!MinecraftForge.EVENT_BUS.post(new ItemGenerationEvent.Artifact(server, inventory, random, stack, fromSlot)))
            inventory.setStackInSlot(fromSlot, ArtifactItem.any(random));
    }
}
