/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileLoadContext;
import ivorius.reccomplex.files.FileTypeHandler;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class ItemCollectionSaveHandler implements FileTypeHandler
{
    public static final ItemCollectionSaveHandler INSTANCE = new ItemCollectionSaveHandler();

    public static final String FILE_SUFFIX = "rcig";

    public static Component readInventoryGenerator(Path file) throws IOException, InventoryLoadException
    {
        return GenericItemCollectionRegistry.INSTANCE.createComponentFromJSON(new String(Files.readAllBytes(file)));
    }

    @Override
    public boolean loadFile(Path path, FileLoadContext context)
    {
        try
        {
            Component component = readInventoryGenerator(path);

            String name = context.customID != null ? context.customID : FilenameUtils.getBaseName(path.getFileName().toString());

            if (component.inventoryGeneratorID == null || component.inventoryGeneratorID.length() == 0) // Legacy support
                component.inventoryGeneratorID = name;

            GenericItemCollectionRegistry.INSTANCE.register(component, name, context.domain, context.active, context.custom);

            return true;
        }
        catch (IOException | InventoryLoadException e)
        {
            RecurrentComplex.logger.warn("Error reading inventory generator", e);
        }

        return false;
    }

    @Override
    public void clearCustomFiles()
    {
        GenericItemCollectionRegistry.INSTANCE.clearCustom();
    }

    public static boolean saveInventoryGenerator(Component info, String name)
    {
        File structuresFile = IvFileHelper.getValidatedFolder(RecurrentComplex.proxy.getBaseFolderFile("structures"));
        if (structuresFile != null)
        {
            File inventoryGeneratorsFile = IvFileHelper.getValidatedFolder(structuresFile, "active");
            if (inventoryGeneratorsFile != null)
            {
                File newFile = new File(inventoryGeneratorsFile, String.format("%s.%s", name, FILE_SUFFIX));

                // Guard against path traversal: name may originate from a network packet
                try
                {
                    if (!newFile.getCanonicalPath().startsWith(inventoryGeneratorsFile.getCanonicalPath() + File.separator))
                    {
                        RecurrentComplex.logger.error("Refusing to save inventory generator outside its directory: " + name);
                        return false;
                    }
                }
                catch (IOException e)
                {
                    RecurrentComplex.logger.error("Error validating inventory generator path: " + name, e);
                    return false;
                }

                String json = GenericItemCollectionRegistry.INSTANCE.createJSONFromComponent(info);

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
}
