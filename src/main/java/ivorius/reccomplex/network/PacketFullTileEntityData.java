/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketFullTileEntityData implements IMessage
{
    private int x;
    private int y;
    private int z;
    private NBTTagCompound data;

    public PacketFullTileEntityData()
    {
    }

    public PacketFullTileEntityData(int x, int y, int z, NBTTagCompound data)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.data = data;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getZ()
    {
        return z;
    }

    public void setZ(int z)
    {
        this.z = z;
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
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeTag(buf, data);
    }
}
