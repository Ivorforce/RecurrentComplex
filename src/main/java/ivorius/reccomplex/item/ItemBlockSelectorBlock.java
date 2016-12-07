/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.item;

import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemBlockSelectorBlock extends ItemBlockSelector
{
    public ItemBlockSelectorBlock()
    {
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (worldIn.isRemote)
            sendClickToServer(stack, worldIn, playerIn, pos);

        return EnumActionResult.SUCCESS;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced)
    {
        super.addInformation(stack, player, list, advanced);

        list.add("(Hold ctrl for secondary selection)");
    }
}
