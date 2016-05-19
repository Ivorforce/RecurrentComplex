/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.network.PacketEditTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

/**
 * Created by lukas on 06.06.14.
 */
public class BlockSpawnScript extends Block
{
    public BlockSpawnScript()
    {
        super(Material.iron);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote && playerIn instanceof EntityPlayerMP && playerIn.canCommandSenderUseCommand(2, ""))
        {
            TileEntity tileEntity = worldIn.getTileEntity(pos);

            RecurrentComplex.network.sendTo(new PacketEditTileEntity((TileEntitySpawnScript) tileEntity), (EntityPlayerMP) playerIn);
        }

        return true;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player)
    {
        TileEntity tileEntity = world.getTileEntity(pos);
        NBTTagCompound compound = new NBTTagCompound();
        ((TileEntitySpawnScript) tileEntity).writeSyncedNBT(compound);

        ItemStack returnStack = new ItemStack(Item.getItemFromBlock(this));
        returnStack.setTagInfo("scriptInfo", compound);

        return returnStack;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("scriptInfo", Constants.NBT.TAG_COMPOUND))
        {
            NBTTagCompound compound = stack.getTagCompound().getCompoundTag("scriptInfo");
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            ((TileEntitySpawnScript) tileEntity).readSyncedNBT(compound);
        }
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEntitySpawnScript();
    }
}
