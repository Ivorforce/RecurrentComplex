/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.EncoderException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Created by lukas on 30.01.17.
 */
public class RCPacketBuffer extends PacketBuffer
{
    public RCPacketBuffer(ByteBuf wrapped)
    {
        super(wrapped);
    }

    @Nullable
    public NBTTagCompound readBigTag() throws IOException
    {
        int i = this.readerIndex();
        byte b0 = this.readByte();

        if (b0 == 0)
        {
            return null;
        }
        else
        {
            this.readerIndex(i);

            try
            {
                return CompressedStreamTools.read(new ByteBufInputStream(this), new NBTSizeTracker(2097152L * 4));
            }
            catch (IOException ioexception)
            {
                throw new EncoderException(ioexception);
            }
        }
    }
}
