/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Created by lukas on 07.05.16.
 */
public class ByteArrays
{
    public static byte[] completeByteArray(InputStream inputStream)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int aByte;

        try
        {
            while ((aByte = inputStream.read()) >= 0)
            {
                byteArrayOutputStream.write(aByte);
            }
        }
        catch (Exception ignored)
        {
            return null;
        }

        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] toByteArray(IOConsumer<ByteArrayOutputStream> consumer) throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        consumer.accept(stream);
        return stream.toByteArray();
    }

    @FunctionalInterface
    public interface IOConsumer<T>
    {
        void accept(T o) throws IOException;

        default IOConsumer<T> andThen(IOConsumer<? super T> after)
        {
            Objects.requireNonNull(after);
            return (T t) -> {
                accept(t);
                after.accept(t);
            };
        }
    }
}
