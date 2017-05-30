/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by lukas on 30.05.17.
 */
public class IvZips
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

    public static void addZipEntry(ZipOutputStream zip, String path, byte[] bytes) throws IOException
    {
        ZipEntry jsonEntry = new ZipEntry(path);
        zip.putNextEntry(jsonEntry);
        jsonEntry.setSize(bytes.length);
        zip.write(bytes);
        zip.closeEntry();
    }

    @FunctionalInterface
    public interface Consumer<T>
    {
        void accept(String name, T t) throws IOException;
    }
}
