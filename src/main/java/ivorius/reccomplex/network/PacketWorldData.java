/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.network.IvPacketHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketWorldData implements IMessage
{
    protected NBTTagCompound worldData;

    protected BlockPos source;
    protected BlockPos capturePoint1;
    protected BlockPos capturePoint2;

    public PacketWorldData()
    {
    }

    public PacketWorldData(NBTTagCompound worldData, BlockPos source, BlockPos capturePoint1, BlockPos capturePoint2)
    {
        this.worldData = worldData;
        this.source = source;
        this.capturePoint1 = capturePoint1;
        this.capturePoint2 = capturePoint2;
    }

    public NBTTagCompound getWorldData()
    {
        return worldData;
    }

    public void setWorldData(NBTTagCompound worldData)
    {
        this.worldData = worldData;
    }

    public BlockPos getCapturePoint1()
    {
        return capturePoint1;
    }

    public void setCapturePoint1(BlockPos capturePoint1)
    {
        this.capturePoint1 = capturePoint1;
    }

    public BlockPos getCapturePoint2()
    {
        return capturePoint2;
    }

    public void setCapturePoint2(BlockPos capturePoint2)
    {
        this.capturePoint2 = capturePoint2;
    }

    public BlockPos getSource()
    {
        return source;
    }

    public void setSource(BlockPos source)
    {
        this.source = source;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        worldData = ByteBufUtils.readTag(buf);

        source = IvPacketHelper.maybeRead(buf, null, () -> BlockPositions.readFromBuffer(buf));
        capturePoint1 = IvPacketHelper.maybeRead(buf, null, () -> BlockPositions.readFromBuffer(buf));
        capturePoint2 = IvPacketHelper.maybeRead(buf, null, () -> BlockPositions.readFromBuffer(buf));
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, worldData);

        IvPacketHelper.maybeWrite(buf, source, () -> BlockPositions.writeToBuffer(source, buf));
        IvPacketHelper.maybeWrite(buf, capturePoint1, () -> BlockPositions.writeToBuffer(capturePoint1, buf));
        IvPacketHelper.maybeWrite(buf, capturePoint2, () -> BlockPositions.writeToBuffer(capturePoint2, buf));
    }
}
