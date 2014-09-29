/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import com.google.gson.JsonSyntaxException;
import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.schematics.SchematicLoader;
import ivorius.reccomplex.worldgen.genericStructures.GenericStructureInfo;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by lukas on 25.05.14.
 */
public class StructureSaveHandler
{
    private static List<String> importedGenerators = new ArrayList<>();

    public static void reloadAllCustomStructures()
    {
        while (!importedGenerators.isEmpty())
        {
            StructureHandler.removeStructure(importedGenerators.remove(0));
        }

        File structuresFile = IvFileHelper.getValidatedFolder(RecurrentComplex.proxy.getBaseFolderFile("structures"));
        if (structuresFile != null)
        {
            File genericStructuresFile = IvFileHelper.getValidatedFolder(structuresFile, "genericStructures");
            if (genericStructuresFile != null)
                addAllStructuresInDirectory(genericStructuresFile, true);

            File silentStructuresFile = IvFileHelper.getValidatedFolder(structuresFile, "silentStructures");
            if (silentStructuresFile != null)
                addAllStructuresInDirectory(silentStructuresFile, false);
        }

        SchematicLoader.initializeFolder();
    }

    public static void addAllStructuresInDirectory(File directory, boolean generating)
    {
        Collection<File> files = FileUtils.listFiles(directory, new String[]{RecurrentComplex.USE_ZIP_FOR_STRUCTURE_FILES ? "zip" : "json"}, true);

        for (File file : files)
        {
            try
            {
                GenericStructureInfo genericStructureInfo = StructureSaveHandler.readGenericStructure(file);

                String structureID = FilenameUtils.getBaseName(file.getName());
                StructureHandler.registerStructure(genericStructureInfo, structureID, generating);
                importedGenerators.add(structureID);
            }
            catch (IOException | StructureInvalidZipException | JsonSyntaxException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static boolean saveGenericStructure(GenericStructureInfo info, String structureName)
    {
        File structuresFolder = RecurrentComplex.proxy.getBaseFolderFile("structures");
        File parent = IvFileHelper.getValidatedFolder(structuresFolder, "silentStructures");
        if (parent != null)
        {
            String json = StructureHandler.createJSONFromStructure(info);

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

    public static GenericStructureInfo readGenericStructure(File file) throws IOException, StructureInvalidZipException, JsonSyntaxException
    {
        try (ZipInputStream zip = new ZipInputStream(FileUtils.openInputStream(file)))
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

    public static GenericStructureInfo structureInfoFromZip(ZipInputStream zipInputStream) throws IOException, StructureInvalidZipException, JsonSyntaxException
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

        GenericStructureInfo genericStructureInfo = StructureHandler.createStructureFromJSON(json);
        genericStructureInfo.worldDataCompound = worldData;

        return genericStructureInfo;
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

    public static class StructureInvalidZipException extends RuntimeException
    {
        public boolean jsonExists;
        public boolean worldDataExists;

        public StructureInvalidZipException(boolean jsonExists, boolean worldDataExists)
        {
            super("Cannot load structure! Found Json: " + jsonExists + ", World Data: " + worldDataExists);
            this.jsonExists = jsonExists;
            this.worldDataExists = worldDataExists;
        }
    }
}
