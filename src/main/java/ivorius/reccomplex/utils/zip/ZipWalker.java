/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by lukas on 30.05.17.
 */
public class ZipWalker
{
    public static void walkStreams(ZipInputStream zipInputStream, Consumer<InputStream> consumer) throws IOException
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
    public interface Consumer<T>
    {
        void accept(String name, T t) throws IOException;
    }
}
