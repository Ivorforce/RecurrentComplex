/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.blocks.BlockPositions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketInspectBlock implements IMessage
{
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
        pos = BlockPositions.readFromBuffer(buf);
        state = ivorius.ivtoolkit.blocks.BlockStates.readBlockState(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        BlockPositions.writeToBuffer(pos, buf);
        ivorius.ivtoolkit.blocks.BlockStates.writeBlockState(buf, state);
    }
}
