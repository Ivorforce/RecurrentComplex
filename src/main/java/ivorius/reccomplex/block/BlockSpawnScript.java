/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.block;

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
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by lukas on 06.06.14.
 */
public class BlockSpawnScript extends Block
{
    public BlockSpawnScript()
    {
        super(Material.IRON);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote && playerIn instanceof EntityPlayerMP && playerIn.canCommandSenderUseCommand(2, ""))
        {
            TileEntity tileEntity = worldIn.getTileEntity(pos);

            RecurrentComplex.network.sendTo(new PacketEditTileEntity((TileEntitySpawnScript) tileEntity), (EntityPlayerMP) playerIn);
        }

        return true;
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
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

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEntitySpawnScript();
    }
}
