/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.items;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.network.PacketInspectBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by lukas on 27.08.16.
 */
public class ItemInspector extends Item
{
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        boolean canUse = playerIn.canCommandSenderUseCommand(2, "setblock");

        if (!worldIn.isRemote)
            RecurrentComplex.network.sendTo(new PacketInspectBlock(pos, worldIn.getBlockState(pos)), (EntityPlayerMP) playerIn);

        return canUse ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
    }
}
