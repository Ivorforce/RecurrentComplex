package ivorius.ivtoolkit.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by lukas on 01.07.14.
 */
public class PacketTileEntityData implements IMessage
{
    private int x, y, z;
    private String context;
    private ByteBuf payload;

    public PacketTileEntityData()
    {
    }

    public PacketTileEntityData(int x, int y, int z, String context, ByteBuf payload)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.context = context;
        this.payload = payload;
    }

    public static <UTileEntity extends TileEntity & PartialUpdateHandler> PacketTileEntityData packetEntityData(UTileEntity entity, String context)
    {
        ByteBuf buf = Unpooled.buffer();
        entity.writeUpdateData(buf, context);
        return new PacketTileEntityData(entity.xCoord, entity.yCoord, entity.zCoord, context, buf);
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

    public String getContext()
    {
        return context;
    }

    public void setContext(String context)
    {
        this.context = context;
    }

    public ByteBuf getPayload()
    {
        return payload;
    }

    public void setPayload(ByteBuf payload)
    {
        this.payload = payload;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        context = ByteBufUtils.readUTF8String(buf);
        payload = IvPacketHelper.readByteBuffer(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, context);
        IvPacketHelper.writeByteBuffer(buf, payload);
    }

}
