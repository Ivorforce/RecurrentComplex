package ivorius.ivtoolkit.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.IExtendedEntityProperties;

/**
 * Created by lukas on 01.07.14.
 */
public class PacketExtendedEntityPropertiesData implements IMessage
{
    private int entityID;
    private String context;
    private String eepKey;
    private ByteBuf payload;

    public PacketExtendedEntityPropertiesData()
    {
    }

    public PacketExtendedEntityPropertiesData(int entityID, String context, String eepKey, ByteBuf payload)
    {
        this.entityID = entityID;
        this.context = context;
        this.eepKey = eepKey;
        this.payload = payload;
    }

    public static PacketExtendedEntityPropertiesData packetEntityData(Entity entity, String eepKey, String context)
    {
        IExtendedEntityProperties eep = entity.getExtendedProperties(eepKey);

        if (!(eep instanceof PartialUpdateHandler))
            throw new IllegalArgumentException("IExtendedEntityProperties must implement IExtendedEntityPropertiesUpdateData to send update packets!");

        ByteBuf buf = Unpooled.buffer();
        ((PartialUpdateHandler) eep).writeUpdateData(buf, context);

        return new PacketExtendedEntityPropertiesData(entity.getEntityId(), context, eepKey, buf);
    }

    public int getEntityID()
    {
        return entityID;
    }

    public void setEntityID(int entityID)
    {
        this.entityID = entityID;
    }

    public String getContext()
    {
        return context;
    }

    public void setContext(String context)
    {
        this.context = context;
    }

    public String getEepKey()
    {
        return eepKey;
    }

    public void setEepKey(String eepKey)
    {
        this.eepKey = eepKey;
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
        entityID = buf.readInt();
        context = ByteBufUtils.readUTF8String(buf);
        eepKey = ByteBufUtils.readUTF8String(buf);
        payload = IvPacketHelper.readByteBuffer(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(entityID);
        ByteBufUtils.writeUTF8String(buf, context);
        ByteBufUtils.writeUTF8String(buf, eepKey);
        IvPacketHelper.writeByteBuffer(buf, payload);
    }

}
