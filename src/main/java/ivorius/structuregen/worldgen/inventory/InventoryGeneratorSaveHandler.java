/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.worldgen.inventory;

import ivorius.structuregen.StructureGen;
import ivorius.structuregen.ivtoolkit.tools.IvFileHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class InventoryGeneratorSaveHandler
{
    private static List<String> importedCustomGenerators = new ArrayList<String>();

    public static void reloadAllCustomInventoryGenerators()
    {
        while (!importedCustomGenerators.isEmpty())
        {
            InventoryGenerationHandler.removeGenerator(importedCustomGenerators.remove(0));
        }
        importedCustomGenerators.clear();

        File structuresFile = IvFileHelper.getValidatedFolder(StructureGen.proxy.getBaseFolderFile("structures"));
        if (structuresFile != null)
        {
            File inventoryGeneratorsFile = IvFileHelper.getValidatedFolder(structuresFile, "inventoryGenerators");
            if (inventoryGeneratorsFile != null)
            {
                File[] inventoryGenFiles = inventoryGeneratorsFile.listFiles();

                if (inventoryGenFiles != null)
                {
                    for (File inventoryGenFile : inventoryGenFiles)
                    {
                        String name = FilenameUtils.getBaseName(inventoryGenFile.getName());
                        GenericInventoryGenerator genericStructureInfo = readInventoryGenerator(inventoryGenFile);

                        if (genericStructureInfo != null)
                        {
                            InventoryGenerationHandler.registerInventoryGenerator(genericStructureInfo, name);
                            importedCustomGenerators.add(name);
                        }
                    }
                }
            }
        }
    }

    public static boolean saveInventoryGenerator(GenericInventoryGenerator info, String name)
    {
        File structuresFile = IvFileHelper.getValidatedFolder(StructureGen.proxy.getBaseFolderFile("structures"));
        if (structuresFile != null)
        {
            File inventoryGeneratorsFile = IvFileHelper.getValidatedFolder(structuresFile, "inventoryGenerators");
            if (inventoryGeneratorsFile != null)
            {
                File newFile = new File(inventoryGeneratorsFile, name + ".json");
                String json = InventoryGenerationHandler.createJSONFromInventoryGenerator(info);

                try
                {
                    FileUtils.writeStringToFile(newFile, json);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                return newFile.exists();
            }
        }

        return false;
    }

    public static GenericInventoryGenerator readInventoryGenerator(ResourceLocation resourceLocation)
    {
        try
        {
            String json = IOUtils.toString(IvFileHelper.inputStreamFromResourceLocation(resourceLocation), "UTF-8");
            return InventoryGenerationHandler.createInventoryGeneratorFromJSON(json);
        }
        catch (Exception ex)
        {
            StructureGen.logger.error("Could not read inventory generator " + resourceLocation.toString(), ex);
        }

        return null;
    }

    public static GenericInventoryGenerator readInventoryGenerator(File file)
    {
        String fullFileName = file.getName();

        String ext = FilenameUtils.getExtension(fullFileName);
        if (file.isFile() && "json".equals(ext))
        {
            String fileContents = null;
            try
            {
                fileContents = FileUtils.readFileToString(file);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if (fileContents != null)
            {
                return InventoryGenerationHandler.createInventoryGeneratorFromJSON(fileContents);
            }
        }

        return null;
    }
}
