/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import com.google.gson.JsonSyntaxException;
import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileSuffixFilter;
import ivorius.reccomplex.files.RCFileHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class InventoryGeneratorSaveHandler
{
    private static List<String> importedCustomGenerators = new ArrayList<>();

    public static void reloadAllCustomInventoryGenerators()
    {
        while (!importedCustomGenerators.isEmpty())
        {
            InventoryGenerationHandler.removeGenerator(importedCustomGenerators.remove(0));
        }
        importedCustomGenerators.clear();

        File structuresFile = IvFileHelper.getValidatedFolder(RecurrentComplex.proxy.getBaseFolderFile("structures"));
        if (structuresFile != null)
        {
            try
            {
                File inventoryGeneratorsFile = IvFileHelper.getValidatedFolder(structuresFile, "inventoryGenerators");
                if (inventoryGeneratorsFile != null)
                    loadAllInventoryGeneratorsInDirectory(inventoryGeneratorsFile.toPath(), true);
            }
            catch (IOException e)
            {
                System.out.println("Could not read from inventory generators directory");
                e.printStackTrace();
            }
        }
    }

    public static void loadInventoryGeneratorsFromMod(String modid)
    {
        modid = modid.toLowerCase();

        try
        {
            Path path = RCFileHelper.pathFromResourceLocation(new ResourceLocation(modid, "structures/inventoryGenerators"));
            if (path != null)
            {
                loadAllInventoryGeneratorsInDirectory(path);
                path.getFileSystem().close();
            }
        }
        catch (URISyntaxException | IOException e)
        {
            System.out.println("Could not read inventory generators from mod '" + modid + "'");
            e.printStackTrace();
        }
    }

    public static void loadAllInventoryGeneratorsInDirectory(Path directory, boolean imported) throws IOException
    {
        List<Path> paths = RCFileHelper.listFilesRecursively(directory, new FileSuffixFilter("json"), true);

        for (Path file : paths)
        {
            try
            {
                GenericInventoryGenerator genericStructureInfo = readInventoryGenerator(file);

                String name = FilenameUtils.getBaseName(file.getFileName().toString());
                InventoryGenerationHandler.registerInventoryGenerator(genericStructureInfo, name);

                if (imported)
                    importedCustomGenerators.add(name);
            }
            catch (IOException | InventoryLoadException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static boolean saveInventoryGenerator(GenericInventoryGenerator info, String name)
    {
        File structuresFile = IvFileHelper.getValidatedFolder(RecurrentComplex.proxy.getBaseFolderFile("structures"));
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
            RecurrentComplex.logger.error("Could not read inventory generator " + resourceLocation.toString(), ex);
        }

        return null;
    }

    public static GenericInventoryGenerator readInventoryGenerator(Path file) throws IOException, InventoryLoadException
    {
        return InventoryGenerationHandler.createInventoryGeneratorFromJSON(new String(Files.readAllBytes(file)));
    }
}
