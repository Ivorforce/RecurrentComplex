/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.blocks.BlockPositions;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketFullTileEntityData implements IMessage
{
    @Nonnull
    private BlockPos pos = BlockPos.ORIGIN;
    private NBTTagCompound data;

    public PacketFullTileEntityData()
    {
    }

    public PacketFullTileEntityData(@Nonnull BlockPos pos, NBTTagCompound data)
    {
        this.pos = pos;
        this.data = data;
    }

    @Nonnull
    public BlockPos getPos()
    {
        return pos;
    }

    public void setPos(@Nonnull BlockPos pos)
    {
        this.pos = pos;
    }

    public NBTTagCompound getData()
    {
        return data;
    }

    public void setData(NBTTagCompound data)
    {
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        pos = BlockPositions.readFromBuffer(buf);
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        BlockPositions.writeToBuffer(pos, buf);
        ByteBufUtils.writeTag(buf, data);
    }
}
