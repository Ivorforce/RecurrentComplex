/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditStructure implements IMessage
{
    private GenericStructureInfo structureInfo;
    private String structureID;

    private Set<String> structuresInActive;
    private Set<String> structuresInInactive;

    public PacketEditStructure()
    {
    }

    public PacketEditStructure(GenericStructureInfo structureInfo, String structureID, Set<String> structuresInActive, Set<String> structuresInInactive)
    {
        this.structureInfo = structureInfo;
        this.structureID = structureID;
        this.structuresInActive = structuresInActive;
        this.structuresInInactive = structuresInInactive;
    }

    public static <T> void writeCollection(ByteBuf buf, Collection<T> collection, Consumer<T> consumer)
    {
        buf.writeInt(collection.size());
        collection.forEach(consumer);
    }

    public static <T> Collection<T> readCollection(ByteBuf buf, Function<ByteBuf, T> supplier)
    {
        return IntStream.range(0, buf.readInt()).mapToObj(i -> supplier.apply(buf)).collect(Collectors.toList());
    }

    public String getStructureID()
    {
        return structureID;
    }

    public void setStructureID(String structureID)
    {
        this.structureID = structureID;
    }

    public GenericStructureInfo getStructureInfo()
    {
        return structureInfo;
    }

    public void setStructureInfo(GenericStructureInfo structureInfo)
    {
        this.structureInfo = structureInfo;
    }

    public Set<String> getStructuresInActive()
    {
        return structuresInActive;
    }

    public void setStructuresInActive(Set<String> structuresInActive)
    {
        this.structuresInActive = structuresInActive;
    }

    public Set<String> getStructuresInInactive()
    {
        return structuresInInactive;
    }

    public void setStructuresInInactive(Set<String> structuresInInactive)
    {
        this.structuresInInactive = structuresInInactive;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        structureID = ByteBufUtils.readUTF8String(buf);
        structureInfo = StructureRegistry.INSTANCE.createStructureFromJSON(ByteBufUtils.readUTF8String(buf));
        structuresInActive = readCollection(buf, ByteBufUtils::readUTF8String).stream().collect(Collectors.toSet());
        structuresInInactive = readCollection(buf, ByteBufUtils::readUTF8String).stream().collect(Collectors.toSet());
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, structureID);
        ByteBufUtils.writeUTF8String(buf, StructureRegistry.INSTANCE.createJSONFromStructure(structureInfo));
        writeCollection(buf, structuresInActive, s -> ByteBufUtils.writeUTF8String(buf, s));
        writeCollection(buf, structuresInInactive, s -> ByteBufUtils.writeUTF8String(buf, s));
    }
}
