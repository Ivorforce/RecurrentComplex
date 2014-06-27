/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.worldgen;

import ivorius.structuregen.StructureGen;
import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.structuregen.worldgen.genericStructures.GenericStructureInfo;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
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
    private static List<String> importedGenerators = new ArrayList<>();

    public static void reloadAllCustomStructures()
    {
        while (!importedGenerators.isEmpty())
        {
            StructureHandler.removeStructure(importedGenerators.remove(0));
        }

        File structuresFile = IvFileHelper.getValidatedFolder(StructureGen.proxy.getBaseFolderFile("structures"));
        if (structuresFile != null)
        {
            File genericStructuresFile = IvFileHelper.getValidatedFolder(structuresFile, "genericStructures");
            if (genericStructuresFile != null)
            {
                addAllStructuresInFolder(genericStructuresFile, true);
            }

            File silentStructuresFile = IvFileHelper.getValidatedFolder(structuresFile, "silentStructures");
            if (silentStructuresFile != null)
            {
                addAllStructuresInFolder(silentStructuresFile, false);
            }
        }
    }

    public static void addAllStructuresInFolder(File folder, boolean generating)
    {
        File[] strucFiles = folder.listFiles();

        if (strucFiles != null)
        {
            for (File strucFile : strucFiles)
            {
                String[] structureNamePointer = new String[1];
                GenericStructureInfo genericStructureInfo = StructureSaveHandler.readGenericStructure(strucFile, structureNamePointer);

                if (genericStructureInfo != null)
                {
                    StructureHandler.registerStructure(genericStructureInfo, structureNamePointer[0], generating);
                    importedGenerators.add(structureNamePointer[0]);
                }
            }
        }
    }

    public static boolean saveGenericStructure(GenericStructureInfo info, String structureName)
    {
        File structuresFolder = StructureGen.proxy.getBaseFolderFile("structures");
        File parent = IvFileHelper.getValidatedFolder(structuresFolder, "silentStructures");
        if (parent != null)
        {
            String json = StructureHandler.createJSONFromStructure(info);

            if (StructureGen.USE_ZIP_FOR_STRUCTURE_FILES)
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
                    StructureGen.logger.error("Could not write structure to zip file", ex);
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

    public static GenericStructureInfo readGenericStructure(File file, String[] structureNamePointer)
    {
        String fullFileName = file.getName();

        if (structureNamePointer != null)
        {
            structureNamePointer[0] = FilenameUtils.getBaseName(fullFileName);
        }

        String ext = FilenameUtils.getExtension(fullFileName);
        if (file.isFile())
        {
            if ("zip".equals(ext))
            {
                ZipInputStream zipInputStream = null;

                try
                {
                    zipInputStream = new ZipInputStream(new FileInputStream(file));
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }

                if (zipInputStream != null)
                {
                    return structureInfoFromZip(zipInputStream, fullFileName);
                }
            }
        }

        return null;
    }

    public static GenericStructureInfo structureInfoFromResource(ResourceLocation resourceLocation)
    {
        try
        {
            return structureInfoFromZip(new ZipInputStream(IvFileHelper.inputStreamFromResourceLocation(resourceLocation)), resourceLocation.toString());
        }
        catch (Exception ex)
        {
            StructureGen.logger.error("Could not read generic structure " + resourceLocation.toString(), ex);
        }

        return null;
    }

    public static GenericStructureInfo structureInfoFromZip(ZipInputStream zipInputStream, String fullFileName)
    {
        String json = null;
        NBTTagCompound worldData = null;

        try
        {
            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null)
            {
                byte[] bytes = completeByteArray(zipInputStream);

                if (bytes != null)
                {
                    if ("structure.json".equals(zipEntry.getName()))
                    {
                        json = new String(bytes);
                    }
                    else if ("worldData.nbt".equals(zipEntry.getName()))
                    {
                        worldData = CompressedStreamTools.decompress(bytes);
                    }
                }

                zipInputStream.closeEntry();
            }
            zipInputStream.close();

            if (json != null)
            {
                GenericStructureInfo genericStructureInfo = StructureHandler.createStructureFromJSON(json);

                if (worldData != null)
                {
                    genericStructureInfo.worldDataCompound = worldData;
                }

                if (genericStructureInfo.worldDataCompound == null)
                {
                    StructureGen.logger.error("Structure file '" + fullFileName + "' does not contain worldData.nbt");
                }
                else
                {
                    return genericStructureInfo;
                }
            }
            else
            {
                StructureGen.logger.error("Structure file '" + fullFileName + "' does not contain structure.json");
            }
        }
        catch (Exception ex)
        {
            StructureGen.logger.error("Could not read structure from zip file", ex);
        }

        return null;
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
