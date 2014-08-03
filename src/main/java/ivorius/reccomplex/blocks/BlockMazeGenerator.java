/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.network.PacketEditMazeBlock;
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
public class BlockMazeGenerator extends BlockContainer implements ITileEntityProvider
{
    public BlockMazeGenerator()
    {
        super(Material.iron);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote && player instanceof EntityPlayerMP && player.canCommandSenderUseCommand(2, ""))
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);

            RecurrentComplex.network.sendTo(new PacketEditMazeBlock((TileEntityMazeGenerator) tileEntity), (EntityPlayerMP) player);
        }

        return true;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        NBTTagCompound compound = new NBTTagCompound();
        ((TileEntityMazeGenerator) tileEntity).writeMazeDataToNBT(compound);

        ItemStack returnStack = new ItemStack(Item.getItemFromBlock(this));
        returnStack.setTagInfo("mazeGeneratorInfo", compound);

        return returnStack;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack)
    {
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("mazeGeneratorInfo"))
        {
            NBTTagCompound compound = itemStack.getTagCompound().getCompoundTag("mazeGeneratorInfo");
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            ((TileEntityMazeGenerator) tileEntity).readMazeDataFromNBT(compound);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityMazeGenerator();
    }
}
