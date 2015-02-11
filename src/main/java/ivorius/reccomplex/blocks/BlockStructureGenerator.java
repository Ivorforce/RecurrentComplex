/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.network.PacketEditTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

/**
 * Created by lukas on 06.06.14.
 */
public class BlockStructureGenerator extends Block
{
    public BlockStructureGenerator()
    {
        super(Material.iron);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote && player instanceof EntityPlayerMP && player.canCommandSenderUseCommand(2, ""))
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);

            RecurrentComplex.network.sendTo(new PacketEditTileEntity((TileEntityStructureGenerator) tileEntity), (EntityPlayerMP) player);
        }

        return true;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        NBTTagCompound compound = new NBTTagCompound();
        ((TileEntityStructureGenerator) tileEntity).writeSyncedNBT(compound);

        ItemStack returnStack = new ItemStack(Item.getItemFromBlock(this));
        returnStack.setTagInfo("structureGeneratorInfo", compound);

        return returnStack;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack)
    {
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("structureGeneratorInfo", Constants.NBT.TAG_COMPOUND))
        {
            NBTTagCompound compound = itemStack.getTagCompound().getCompoundTag("structureGeneratorInfo");
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            ((TileEntityStructureGenerator) tileEntity).readSyncedNBT(compound);
        }
    }

    @Override
    public boolean hasTileEntity(int metadata)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World var1, int var2)
    {
        return new TileEntityStructureGenerator();
    }
}
