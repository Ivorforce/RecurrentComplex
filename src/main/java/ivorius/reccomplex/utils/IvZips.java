/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by lukas on 30.05.17.
 */
public class IvZips
{
    public static void walkStreams(ZipInputStream zipInputStream, ZipEntryConsumer<InputStream> consumer) throws IOException
    {
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null)
        {
            consumer.accept(zipEntry.getName(), zipInputStream);
            zipInputStream.closeEntry();
        }
        zipInputStream.close();
    }

    @FunctionalInterface
    public interface ZipEntryConsumer<T>
    {
        void accept(String name, T t) throws IOException;
    }

    public static class Finder
    {
        private final Map<String, EntryConsumer<InputStream>> map = new HashMap<>();

        public void read(ZipInputStream stream) throws IOException
        {
            walkStreams(stream, (name, inputStream) -> {
                EntryConsumer<InputStream> consumer = map.get(name);
                if (consumer != null)
                    consumer.accept(inputStream);
            });
        }

        public <T> Supplier<T> stream(String name, Function<InputStream, T> function)
        {
            if (map.containsKey(name))
                throw new IllegalArgumentException();

            Object[] result = new Object[1];
            map.put(name, inputStream -> result[0] = function.apply(inputStream));
            //noinspection unchecked
            return () -> (T) result[0];
        }

        public <T> Supplier<T> bytes(String name, Function<byte[], T> function)
        {
            return stream(name, inputStream ->
            {
                byte[] bytes = ByteArrays.completeByteArray(inputStream);
                return bytes != null ? function.apply(bytes) : null;
            });
        }

        @FunctionalInterface
        public interface Function<S, D>
        {
            D apply(S t) throws IOException;
        }

        @FunctionalInterface
        private interface EntryConsumer<T>
        {
            void accept(T t) throws IOException;
        }
    }
}
