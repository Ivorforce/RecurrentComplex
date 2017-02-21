/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.temp;

import ivorius.ivtoolkit.transform.Movable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 21.02.17.
 */
public class RCMover
{
    public static NBTTagCompound setTileEntityPos(NBTTagCompound tileEntityCompound, BlockPos pos)
    {
        NBTTagCompound compound = tileEntityCompound.copy();
        compound.setInteger("x", pos.getX());
        compound.setInteger("y", pos.getY());
        compound.setInteger("z", pos.getZ());
        return compound;
    }

    @Nonnull
    public static BlockPos getTileEntityPos(NBTTagCompound tileEntityCompound)
    {
        return new BlockPos(tileEntityCompound.getInteger("x"), tileEntityCompound.getInteger("y"), tileEntityCompound.getInteger("z"));
    }

    public static void moveAdditionalData(TileEntity tileEntity, BlockPos dist)
    {
        if (tileEntity instanceof Movable)
            ((Movable) tileEntity).move(dist);
    }

    public static void setAdditionalDataPos(TileEntity tileEntity, BlockPos coord)
    {
        moveAdditionalData(tileEntity, coord.subtract(tileEntity.getPos()));
    }
}
