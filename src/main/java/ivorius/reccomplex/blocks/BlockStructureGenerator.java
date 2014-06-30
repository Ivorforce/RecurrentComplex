/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
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

/**
 * Created by lukas on 06.06.14.
 */
public class BlockStructureGenerator extends BlockContainer implements ITileEntityProvider
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

            RecurrentComplex.chEditStructureBlock.sendBeginEdit(((EntityPlayerMP) player), ((TileEntityStructureGenerator) tileEntity));
        }

        return true;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        NBTTagCompound compound = new NBTTagCompound();
        ((TileEntityStructureGenerator) tileEntity).writeStructureDataToNBT(compound);

        ItemStack returnStack = new ItemStack(Item.getItemFromBlock(this));
        returnStack.setTagInfo("structureGeneratorInfo", compound);

        return returnStack;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack)
    {
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("structureGeneratorInfo"))
        {
            NBTTagCompound compound = itemStack.getTagCompound().getCompoundTag("structureGeneratorInfo");
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            ((TileEntityStructureGenerator) tileEntity).readStructureDataFromNBT(compound);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityStructureGenerator();
    }
}
