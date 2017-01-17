/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.network.IvPacketHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketInspectBlock implements IMessage
{
    private NBTTagCompound tileEntityData;
    private BlockPos pos;
    private IBlockState state;

    public PacketInspectBlock()
    {
    }

    public PacketInspectBlock(BlockPos pos, IBlockState state)
    {
        this.pos = pos;
        this.state = state;
    }

    public PacketInspectBlock(NBTTagCompound tileEntityData, BlockPos pos, IBlockState state)
    {
        this.tileEntityData = tileEntityData;
        this.pos = pos;
        this.state = state;
    }

    public NBTTagCompound getTileEntityData()
    {
        return tileEntityData;
    }

    public void setTileEntityData(NBTTagCompound tileEntityData)
    {
        this.tileEntityData = tileEntityData;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public void setPos(BlockPos pos)
    {
        this.pos = pos;
    }

    public IBlockState getState()
    {
        return state;
    }

    public void setState(IBlockState state)
    {
        this.state = state;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        tileEntityData = IvPacketHelper.maybeRead(buf, null, () -> ByteBufUtils.readTag(buf));
        pos = BlockPositions.readFromBuffer(buf);
        state = ivorius.ivtoolkit.blocks.BlockStates.readBlockState(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        IvPacketHelper.maybeWrite(buf, tileEntityData, () -> ByteBufUtils.writeTag(buf, tileEntityData));
        BlockPositions.writeToBuffer(pos, buf);
        ivorius.ivtoolkit.blocks.BlockStates.writeBlockState(buf, state);
    }
}
