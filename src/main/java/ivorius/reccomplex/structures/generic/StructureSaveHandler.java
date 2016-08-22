/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

import com.google.common.collect.Sets;
import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileLoadContext;
import ivorius.reccomplex.files.FileTypeHandler;
import ivorius.reccomplex.files.RCFileTypeRegistry;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.utils.ByteArrays;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.*;
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
public class StructureSaveHandler implements FileTypeHandler
{
    public static final StructureSaveHandler INSTANCE = new StructureSaveHandler(StructureRegistry.INSTANCE);

    public static final String FILE_SUFFIX = "rcst";
    public static final String STRUCTURE_INFO_JSON_FILENAME = "structure.json";
    public static final String WORLD_DATA_NBT_FILENAME = "worldData.nbt";

    public StructureRegistry registry;

    public StructureSaveHandler(StructureRegistry registry)
    {
        this.registry = registry;
    }

    protected static void addZipEntry(ZipOutputStream zip, String path, byte[] bytes) throws IOException
    {
        ZipEntry jsonEntry = new ZipEntry(path);
        zip.putNextEntry(jsonEntry);
        jsonEntry.setSize(bytes.length);
        zip.write(bytes);
        zip.closeEntry();
    }

    public boolean saveGenericStructure(GenericStructureInfo info, String structureName, boolean activeFolder)
    {
        File parent = RCFileTypeRegistry.getDirectory(activeFolder);
        if (parent != null)
        {
            String json = registry.createJSONFromStructure(info);

            if (RecurrentComplex.USE_ZIP_FOR_STRUCTURE_FILES)
            {
                File newFile = new File(parent, structureName + "." + FILE_SUFFIX);
                boolean failed = false;

                try
                {
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

    public Set<String> listGenericStructures(boolean activeFolder)
    {
        try
        {
            File parent = RCFileTypeRegistry.getDirectory(activeFolder);
            return Arrays.stream(parent.list(FileFilterUtils.or(FileFilterUtils.suffixFileFilter(FILE_SUFFIX), /* Legacy */ FileFilterUtils.suffixFileFilter("zip"))))
                    .map(FilenameUtils::removeExtension)
                    .collect(Collectors.toSet());
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error when looking up structure", e);
        }

        return Collections.emptySet();
    }

    public boolean hasGenericStructure(String structureName, boolean activeFolder)
    {
        try
        {
            File parent = RCFileTypeRegistry.getDirectory(activeFolder);
            return parent != null && (new File(parent, structureName + "." + FILE_SUFFIX).exists() || /* Legacy */ new File(parent, structureName + ".zip").exists());
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error when looking up structure", e);
        }

        return false;
    }

    public boolean deleteGenericStructure(String structureName, boolean activeFolder)
    {
        try
        {
            File parent = RCFileTypeRegistry.getDirectory(activeFolder);
            return parent != null && (new File(parent, structureName + "." + FILE_SUFFIX).delete() || /* Legacy */ new File(parent, structureName + ".zip").delete());
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error when deleting structure", e);
        }

        return false;
    }

    public GenericStructureInfo readGenericStructure(Path file) throws StructureLoadException, IOException
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

            GenericStructureInfo genericStructureInfo = registry.createStructureFromJSON(json);
            genericStructureInfo.worldDataCompound = worldData;

            return genericStructureInfo;
        }
        catch (IOException e)
        {
            throw new StructureLoadException(e);
        }
    }

    @Override
    public boolean loadFile(Path path, FileLoadContext context)
    {
        try
        {
            GenericStructureInfo genericStructureInfo = readGenericStructure(path);

            String structureID = context.customID != null ? context.customID : FilenameUtils.getBaseName(path.getFileName().toString());

            if (registry.registerStructure(genericStructureInfo, structureID, context.domain, context.active, context.custom))
                return true;
        }
        catch (IOException | StructureLoadException e)
        {
            RecurrentComplex.logger.warn("Error reading structure", e);
        }

        return false;
    }

    @Override
    public void clearCustomFiles()
    {
        StructureRegistry.INSTANCE.clearCustom();
    }
}
