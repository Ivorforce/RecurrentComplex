/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileSuffixFilter;
import ivorius.reccomplex.files.RCFileHelper;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class ItemCollectionSaveHandler
{
    public static final String FILE_SUFFIX = "rcig";

    private static List<String> importedCustomGenerators = new ArrayList<>();

    public static void reloadAllCustomInventoryGenerators()
    {
        for (String generator : importedCustomGenerators)
            WeightedItemCollectionRegistry.unregister(generator);
        importedCustomGenerators.clear();

        File structuresFile = IvFileHelper.getValidatedFolder(RecurrentComplex.proxy.getBaseFolderFile("structures"));
        if (structuresFile != null)
        {
            tryAddAllItemCollectionsInDirectory(IvFileHelper.getValidatedFolder(structuresFile, "active"), "", true, true);
            tryAddAllItemCollectionsInDirectory(IvFileHelper.getValidatedFolder(structuresFile, "inactive"), "", false, true);

            // Legacy
            tryAddAllItemCollectionsInDirectory(IvFileHelper.getValidatedFolder(structuresFile, "inventoryGenerators"), "", true, true);
        }
    }

    public static void loadInventoryGeneratorsFromMod(String modid)
    {
        modid = modid.toLowerCase();

        tryAddAllItemCollectionsInResourceLocation(new ResourceLocation(modid, "structures/active"), true, false);
        tryAddAllItemCollectionsInResourceLocation(new ResourceLocation(modid, "structures/inactive"), false, false);

        // Legacy
        tryAddAllItemCollectionsInResourceLocation(new ResourceLocation(modid, "structures/inventoryGenerators"), true, false);
    }

    protected static void tryAddAllItemCollectionsInResourceLocation(ResourceLocation resourceLocation, boolean generating, boolean imported)
    {
        try
        {
            Path path = RCFileHelper.pathFromResourceLocation(resourceLocation);
            if (path != null)
                loadAllInventoryGeneratorsInDirectory(path, resourceLocation.getResourceDomain(), generating, imported);
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error reading from resource location '" + resourceLocation + "'", e);
        }
    }

    protected static void tryAddAllItemCollectionsInDirectory(File file, String domain, boolean generating, boolean imported)
    {
        if (file != null)
        {
            try
            {
                loadAllInventoryGeneratorsInDirectory(file.toPath(), domain, generating, imported);
            }
            catch (Throwable e)
            {
                RecurrentComplex.logger.error("Error reading from directory '" + file + "'", e);
            }
        }
    }

    public static void loadAllInventoryGeneratorsInDirectory(Path directory, String domain, boolean generating, boolean imported) throws IOException
    {
        List<Path> paths = RCFileHelper.listFilesRecursively(directory, new FileSuffixFilter(FILE_SUFFIX, /* Legacy */ "json"), true);

        for (Path file : paths)
        {
            try
            {
                Component component = readInventoryGenerator(file);

                String name = FilenameUtils.getBaseName(file.getFileName().toString());

                if (component.inventoryGeneratorID == null || component.inventoryGeneratorID.length() == 0) // Legacy support
                    component.inventoryGeneratorID = name;

                GenericItemCollectionRegistry.register(component, name, domain, generating);

                if (imported)
                    importedCustomGenerators.add(name);
            }
            catch (IOException | InventoryLoadException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static boolean saveInventoryGenerator(Component info, String name)
    {
        File structuresFile = IvFileHelper.getValidatedFolder(RecurrentComplex.proxy.getBaseFolderFile("structures"));
        if (structuresFile != null)
        {
            File inventoryGeneratorsFile = IvFileHelper.getValidatedFolder(structuresFile, "inventoryGenerators");
            if (inventoryGeneratorsFile != null)
            {
                File newFile = new File(inventoryGeneratorsFile, String.format("%s.%s", name, FILE_SUFFIX));
                String json = GenericItemCollectionRegistry.createJSONFromComponent(info);

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

    public static Component readInventoryGenerator(ResourceLocation resourceLocation)
    {
        try
        {
            String json = IOUtils.toString(IvFileHelper.inputStreamFromResourceLocation(resourceLocation), "UTF-8");
            return GenericItemCollectionRegistry.createComponentFromJSON(json);
        }
        catch (Exception ex)
        {
            RecurrentComplex.logger.error("Could not read inventory generator " + resourceLocation.toString(), ex);
        }

        return null;
    }

    public static Component readInventoryGenerator(Path file) throws IOException, InventoryLoadException
    {
        return GenericItemCollectionRegistry.createComponentFromJSON(new String(Files.readAllBytes(file)));
    }
}
