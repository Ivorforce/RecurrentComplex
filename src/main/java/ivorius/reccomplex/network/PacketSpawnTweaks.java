/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.network.IvPacketHelper;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.StructureSaveHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketSpawnTweaks implements IMessage
{
    private TObjectFloatMap<String> data;

    public PacketSpawnTweaks()
    {
    }

    public PacketSpawnTweaks(TObjectFloatMap<String> data)
    {
        this.data = data;
    }

    public TObjectFloatMap<String> getData()
    {
        return data;
    }

    public void setData(TObjectFloatMap<String> data)
    {
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        int num = buf.readInt();

        data = new TObjectFloatHashMap<>();

        for (int i = 0; i < num; i++) {
            String id = ByteBufUtils.readUTF8String(buf);
            data.put(id, buf.readFloat());
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(data.size());

        data.forEachEntry((id, tweak) -> {
            ByteBufUtils.writeUTF8String(buf, id);
            buf.writeFloat(tweak);

            return true;
        });
    }
}
