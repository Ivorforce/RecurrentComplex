/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.item;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.network.PacketInspectBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        boolean canUse = playerIn.canUseCommand(2, "setblock");

        if (!worldIn.isRemote)
        {
            PacketInspectBlock message = new PacketInspectBlock(pos, worldIn.getBlockState(pos));

            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity != null)
                message.setTileEntityData(tileEntity.writeToNBT(new NBTTagCompound()));

            RecurrentComplex.network.sendTo(message, (EntityPlayerMP) playerIn);
        }

        return canUse ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
    }
}
