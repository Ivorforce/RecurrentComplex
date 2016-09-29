/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileTypeHandlerRegistry;
import ivorius.reccomplex.files.RCFileSuffix;
import ivorius.reccomplex.files.RCFileTypeRegistry;
import ivorius.reccomplex.json.NbtToJson;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.utils.ByteArrays;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by lukas on 25.05.14.
 */
public class StructureSaveHandler
{
    public static final StructureSaveHandler INSTANCE = new StructureSaveHandler(StructureRegistry.INSTANCE);

    public static final String STRUCTURE_INFO_JSON_FILENAME = "structure.json";
    public static final String WORLD_DATA_NBT_FILENAME = "worldData.nbt";

    public final StructureRegistry registry;
    public final Gson gson;

    public StructureSaveHandler(StructureRegistry registry)
    {
        this.registry = registry;
        gson = createGson();
    }

    protected static void addZipEntry(ZipOutputStream zip, String path, byte[] bytes) throws IOException
    {
        ZipEntry jsonEntry = new ZipEntry(path);
        zip.putNextEntry(jsonEntry);
        jsonEntry.setSize(bytes.length);
        zip.write(bytes);
        zip.closeEntry();
    }

    public static Set<String> listFiles(boolean activeFolder, IOFileFilter... filter)
    {
        try
        {
            File parent = RCFileTypeRegistry.getDirectory(activeFolder);
            return Arrays.stream(parent.list(FileFilterUtils.or(filter)))
                    .map(FilenameUtils::removeExtension)
                    .collect(Collectors.toSet());
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error when looking up structure", e);
        }

        return Collections.emptySet();
    }

    public Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(GenericStructureInfo.class, new GenericStructureInfo.Serializer());
        StructureRegistry.TRANSFORMERS.constructGson(builder);
        StructureRegistry.GENERATION_INFOS.constructGson(builder);

        NbtToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public GenericStructureInfo fromJSON(String jsonData) throws JsonSyntaxException
    {
        return gson.fromJson(jsonData, GenericStructureInfo.class);
    }

    public String toJSON(GenericStructureInfo structureInfo)
    {
        return gson.toJson(structureInfo, GenericStructureInfo.class);
    }

    public boolean save(GenericStructureInfo info, String structureName, boolean activeFolder)
    {
        File parent = RCFileTypeRegistry.getDirectory(activeFolder);
        if (parent != null)
        {
            String json = toJSON(info);

            if (RecurrentComplex.USE_ZIP_FOR_STRUCTURE_FILES)
            {
                File newFile = new File(parent, structureName + "." + RCFileSuffix.STRUCTURE);
                boolean failed = false;

                try
                {
                    newFile.delete(); // Prevent case mismatching
                    ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(newFile));

                    addZipEntry(zipOutputStream, STRUCTURE_INFO_JSON_FILENAME, json.getBytes());
                    addZipEntry(zipOutputStream, WORLD_DATA_NBT_FILENAME, ByteArrays.toByteArray(s -> CompressedStreamTools.writeCompressed(info.worldDataCompound, s)));

                    zipOutputStream.close();
                }
                catch (Exception ex)
                {
                    RecurrentComplex.logger.error("Could not write structure to zip file", ex);
                    failed = true;
                }

                return !failed && newFile.exists();
            }
            else
            {
                File newFile = new File(parent, structureName + ".json");
                boolean failed = false;

                try
                {
                    newFile.delete(); // Prevent case mismatching
                    FileUtils.writeStringToFile(newFile, json);
                }
                catch (Exception e)
                {
                    RecurrentComplex.logger.error("Could not write structure zip to folder", e);
                    failed = true;
                }

                return !failed && newFile.exists();
            }
        }

        return false;
    }

    public Set<String> list(boolean activeFolder)
    {
        return listFiles(activeFolder, FileFilterUtils.suffixFileFilter(RCFileSuffix.STRUCTURE), FileFilterUtils.suffixFileFilter("zip"));
    }

    public boolean has(String name, boolean activeFolder)
    {
        try
        {
            File parent = RCFileTypeRegistry.getDirectory(activeFolder);
            return parent != null && (new File(parent, name + "." + RCFileSuffix.STRUCTURE).exists() || /* Legacy */ new File(parent, name + ".zip").exists());
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error when looking up structure", e);
        }

        return false;
    }

    public boolean delete(String name, boolean activeFolder)
    {
        try
        {
            File parent = RCFileTypeRegistry.getDirectory(activeFolder);
            return parent != null && (new File(parent, name + "." + RCFileSuffix.STRUCTURE).delete() || /* Legacy */ new File(parent, name + ".zip").delete());
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error when deleting structure", e);
        }

        return false;
    }

    public GenericStructureInfo read(Path file) throws StructureLoadException, IOException
    {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(file)))
        {
            return structureInfoFromZip(zip);
        }
    }

    public GenericStructureInfo structureInfoFromResource(ResourceLocation resourceLocation)
    {
        try
        {
            return structureInfoFromZip(new ZipInputStream(IvFileHelper.inputStreamFromResourceLocation(resourceLocation)));
        }
        catch (Exception ex)
        {
            RecurrentComplex.logger.error("Could not read generic structure " + resourceLocation.toString(), ex);
        }

        return null;
    }

    public GenericStructureInfo structureInfoFromZip(ZipInputStream zipInputStream) throws StructureLoadException
    {
        try
        {
            String json = null;
            NBTTagCompound worldData = null;

            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null)
            {
                byte[] bytes = ByteArrays.completeByteArray(zipInputStream);

                if (bytes != null)
                {
                    if (STRUCTURE_INFO_JSON_FILENAME.equals(zipEntry.getName()))
                        json = new String(bytes);
                    else if (WORLD_DATA_NBT_FILENAME.equals(zipEntry.getName()))
                        worldData = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
                }

                zipInputStream.closeEntry();
            }
            zipInputStream.close();

            if (json == null || worldData == null)
                throw new StructureInvalidZipException(json != null, worldData != null);

            GenericStructureInfo genericStructureInfo = fromJSON(json);
            genericStructureInfo.worldDataCompound = worldData;

            return genericStructureInfo;
        }
        catch (IOException e)
        {
            throw new StructureLoadException(e);
        }
    }

    public static class Loader extends FileTypeHandlerRegistry<StructureInfo>
    {
        public Loader() {
            super(RCFileSuffix.STRUCTURE, StructureRegistry.INSTANCE);}

        @Override
        public StructureInfo read(Path path, String name) throws Exception
        {
            return INSTANCE.read(path);
        }
    }
}
