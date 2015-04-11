/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.files.FileSuffixFilter;
import ivorius.reccomplex.files.RCFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.schematics.SchematicLoader;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by lukas on 25.05.14.
 */
public class StructureSaveHandler
{
    public static final String ACTIVE_DIR_NAME = "active";
    public static final String INACTIVE_DIR_NAME = "inactive";
    private static List<String> importedGenerators = new ArrayList<>();

    public static String getStructuresDirectoryName(boolean activeFolder)
    {
        return activeFolder ? ACTIVE_DIR_NAME : INACTIVE_DIR_NAME;
    }

    public static File getStructuresDirectory(boolean activeFolder)
    {
        File structuresFolder = RecurrentComplex.proxy.getBaseFolderFile("structures");
        return RCFileHelper.getValidatedFolder(structuresFolder, getStructuresDirectoryName(activeFolder), true);
    }

    public static void reloadAllCustomStructures()
    {
        while (!importedGenerators.isEmpty())
        {
            StructureRegistry.removeStructure(importedGenerators.remove(0));
        }

        File structuresFile = IvFileHelper.getValidatedFolder(RecurrentComplex.proxy.getBaseFolderFile("structures"));
        if (structuresFile != null)
        {
            tryAddAllStructuresInDirectory(RCFileHelper.getValidatedFolder(structuresFile, ACTIVE_DIR_NAME, true), true, true);
            tryAddAllStructuresInDirectory(RCFileHelper.getValidatedFolder(structuresFile, INACTIVE_DIR_NAME, true), false, true);

            // Legacy
            tryAddAllStructuresInDirectory(RCFileHelper.getValidatedFolder(structuresFile, "genericStructures", false), true, true);
            tryAddAllStructuresInDirectory(RCFileHelper.getValidatedFolder(structuresFile, "silentStructures", false), false, true);
        }

        SchematicLoader.initializeFolder();
    }

    public static void loadStructuresFromMod(String modid, boolean disableGeneration)
    {
        modid = modid.toLowerCase();

        tryAddAllStructuresInResourceLocation(new ResourceLocation(modid, "structures/" + ACTIVE_DIR_NAME), !disableGeneration, false);
        tryAddAllStructuresInResourceLocation(new ResourceLocation(modid, "structures/" + INACTIVE_DIR_NAME), false, false);

        // Legacy
        tryAddAllStructuresInResourceLocation(new ResourceLocation(modid, "structures/genericStructures"), !disableGeneration, false);
        tryAddAllStructuresInResourceLocation(new ResourceLocation(modid, "structures/silentStructures"), false, false);
    }

    public static void tryAddAllStructuresInResourceLocation(ResourceLocation resourceLocation, boolean generating, boolean imported)
    {
        try
        {
            Path path = RCFileHelper.pathFromResourceLocation(resourceLocation);
            if (path != null)
                addAllStructuresInDirectory(path, generating, imported);
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error reading from resource location '" + resourceLocation + "'", e);
        }
    }

    public static void tryAddAllStructuresInDirectory(File file, boolean generating, boolean imported)
    {
        if (file != null)
        {
            try
            {
                addAllStructuresInDirectory(file.toPath(), generating, imported);
            }
            catch (Throwable e)
            {
                RecurrentComplex.logger.error("Error reading from directory '" + file + "'", e);
            }
        }
    }

    public static void addAllStructuresInDirectory(Path directory, boolean generating, boolean imported) throws IOException
    {
        List<Path> paths = RCFileHelper.listFilesRecursively(directory, new FileSuffixFilter(RecurrentComplex.USE_ZIP_FOR_STRUCTURE_FILES ? "zip" : "json"), true);

        for (Path file : paths)
        {
            try
            {
                GenericStructureInfo genericStructureInfo = StructureSaveHandler.readGenericStructure(file);

                String structureID = FilenameUtils.getBaseName(file.getFileName().toString());
                StructureRegistry.registerStructure(genericStructureInfo, structureID, generating);

                if (imported)
                    importedGenerators.add(structureID);
            }
            catch (IOException | StructureLoadException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static boolean saveGenericStructure(GenericStructureInfo info, String structureName, boolean activeFolder)
    {
        File parent = getStructuresDirectory(activeFolder);
        if (parent != null)
        {
            String json = StructureRegistry.createJSONFromStructure(info);

            if (RecurrentComplex.USE_ZIP_FOR_STRUCTURE_FILES)
            {
                File newFile = new File(parent, structureName + ".zip");
                boolean failed = false;

                try
                {
                    ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(newFile));

                    ZipEntry jsonEntry = new ZipEntry("structure.json");
                    zipOutputStream.putNextEntry(jsonEntry);
                    byte[] jsonBytes = json.getBytes();
                    jsonEntry.setSize(jsonBytes.length);
                    zipOutputStream.write(jsonBytes);
                    zipOutputStream.closeEntry();

                    ZipEntry worldDataEntry = new ZipEntry("worldData.nbt");
                    zipOutputStream.putNextEntry(worldDataEntry);
                    byte[] worldDataBytes = CompressedStreamTools.compress(info.worldDataCompound);
                    worldDataEntry.setSize(worldDataBytes.length);
                    zipOutputStream.write(worldDataBytes);
                    zipOutputStream.closeEntry();

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
                    e.printStackTrace();
                    failed = true;
                }

                return !failed && newFile.exists();
            }
        }

        return false;
    }

    public static boolean hasGenericStructure(String structureName, boolean activeFolder)
    {
        try
        {
            File parent = getStructuresDirectory(activeFolder);
            return parent != null && new File(parent, structureName + ".zip").exists();
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error when looking up structure", e);
        }

        return false;
    }

    public static boolean deleteGenericStructure(String structureName, boolean activeFolder)
    {
        try
        {
            File parent = getStructuresDirectory(activeFolder);
            return parent != null && new File(parent, structureName + ".zip").delete();
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error when deleting structure", e);
        }

        return false;
    }

    public static GenericStructureInfo readGenericStructure(Path file) throws StructureLoadException, IOException
    {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(file)))
        {
            return structureInfoFromZip(zip);
        }
    }

    public static GenericStructureInfo structureInfoFromResource(ResourceLocation resourceLocation)
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

    public static GenericStructureInfo structureInfoFromZip(ZipInputStream zipInputStream) throws StructureLoadException
    {
        try
        {
            String json = null;
            NBTTagCompound worldData = null;

            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null)
            {
                byte[] bytes = completeByteArray(zipInputStream);

                if (bytes != null)
                {
                    if ("structure.json".equals(zipEntry.getName()))
                        json = new String(bytes);
                    else if ("worldData.nbt".equals(zipEntry.getName()))
                        worldData = CompressedStreamTools.func_152457_a(bytes, NBTSizeTracker.field_152451_a);
                }

                zipInputStream.closeEntry();
            }
            zipInputStream.close();

            if (json == null || worldData == null)
                throw new StructureInvalidZipException(json != null, worldData != null);

            GenericStructureInfo genericStructureInfo = StructureRegistry.createStructureFromJSON(json);
            genericStructureInfo.worldDataCompound = worldData;

            return genericStructureInfo;
        }
        catch (IOException e)
        {
            throw new StructureLoadException(e);
        }
    }

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
}
