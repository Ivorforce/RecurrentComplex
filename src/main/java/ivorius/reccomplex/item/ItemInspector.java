/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.item;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.network.PacketInspectBlock;
import ivorius.reccomplex.network.PacketInspectEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
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
    public static void inspectBlock(EntityPlayerMP playerIn, World worldIn, BlockPos pos)
    {
        PacketInspectBlock message = new PacketInspectBlock(pos, worldIn.getBlockState(pos));

        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity != null)
            message.setTileEntityData(tileEntity.writeToNBT(new NBTTagCompound()));

        RecurrentComplex.network.sendTo(message, playerIn);
    }

    public static void inspectEntity(EntityPlayerMP playerIn, EntityLivingBase target)
    {
        PacketInspectEntity message = new PacketInspectEntity(target.writeToNBT(new NBTTagCompound()), target.getUniqueID(), target.getName());

        RecurrentComplex.network.sendTo(message, playerIn);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote)
            inspectBlock((EntityPlayerMP) playerIn, worldIn, pos);

        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        if (!worldIn.isRemote && ItemBlockSelector.modifierKeyDown())
            inspectEntity((EntityPlayerMP) playerIn, playerIn);

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand)
    {
        World worldIn = playerIn.world;

        if (!worldIn.isRemote)
            inspectEntity((EntityPlayerMP) playerIn, target);

        return true;
    }
}
