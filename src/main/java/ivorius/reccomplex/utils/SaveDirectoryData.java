/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 31.08.16.
 */
public class SaveDirectoryData
{
    private boolean saveAsActive;
    private boolean deleteOther;

    private Set<String> filesInActive;
    private Set<String> filesInInactive;

    public SaveDirectoryData(boolean saveAsActive, boolean deleteOther, Set<String> filesInActive, Set<String> filesInInactive)
    {
        this.saveAsActive = saveAsActive;
        this.deleteOther = deleteOther;
        this.filesInActive = filesInActive;
        this.filesInInactive = filesInInactive;
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

    public static SaveDirectoryData defaultData(String id, Set<String> filesInActive, Set<String> filesInInactive)
    {
        return new SaveDirectoryData(filesInActive.contains(id), true, filesInActive, filesInInactive);
    }

    public static SaveDirectoryData readFrom(ByteBuf buf)
    {
        return new SaveDirectoryData(buf.readBoolean(), buf.readBoolean(),
                readCollection(buf, ByteBufUtils::readUTF8String).stream().collect(Collectors.toSet()),
                readCollection(buf, ByteBufUtils::readUTF8String).stream().collect(Collectors.toSet()));
    }

    public void writeTo(ByteBuf buf)
    {
        buf.writeBoolean(saveAsActive);
        buf.writeBoolean(deleteOther);

        writeCollection(buf, filesInActive, s -> ByteBufUtils.writeUTF8String(buf, s));
        writeCollection(buf, filesInInactive, s -> ByteBufUtils.writeUTF8String(buf, s));
    }

    public boolean isSaveAsActive()
    {
        return saveAsActive;
    }

    public void setSaveAsActive(boolean saveAsActive)
    {
        this.saveAsActive = saveAsActive;
    }

    public boolean isDeleteOther()
    {
        return deleteOther;
    }

    public void setDeleteOther(boolean deleteOther)
    {
        this.deleteOther = deleteOther;
    }

    public Set<String> getFilesInActive()
    {
        return filesInActive;
    }

    public void setFilesInActive(Set<String> filesInActive)
    {
        this.filesInActive = filesInActive;
    }

    public Set<String> getFilesInInactive()
    {
        return filesInInactive;
    }

    public void setFilesInInactive(Set<String> filesInInactive)
    {
        this.filesInInactive = filesInInactive;
    }

    public Result getResult()
    {
        return new Result(isSaveAsActive(), isDeleteOther());
    }

    public static class Result
    {
        public final boolean saveAsActive;
        public final boolean deleteOther;

        public Result(boolean saveAsActive, boolean deleteOther)
        {
            this.saveAsActive = saveAsActive;
            this.deleteOther = deleteOther;
        }

        public static Result readFrom(ByteBuf buf)
        {
            return new Result(buf.readBoolean(), buf.readBoolean());
        }

        public void writeTo(ByteBuf buf)
        {
            buf.writeBoolean(saveAsActive);
            buf.writeBoolean(deleteOther);
        }
    }
}
