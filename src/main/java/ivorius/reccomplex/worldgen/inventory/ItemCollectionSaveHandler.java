/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileLoadContext;
import ivorius.reccomplex.files.FileTypeHandler;
import ivorius.reccomplex.files.RCFileTypeRegistry;
import ivorius.reccomplex.structures.generic.StructureSaveHandler;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

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

    public boolean save(@Nonnull Component info, @Nonnull String name, boolean active)
    {
        File parent = RCFileTypeRegistry.getDirectory(active);
        if (parent != null)
        {
            File newFile = new File(parent, String.format("%s.%s", name, FILE_SUFFIX));
            String json = GenericItemCollectionRegistry.INSTANCE.createJSONFromComponent(info);

            try
            {
                newFile.delete(); // Prevent case mismatching
                FileUtils.writeStringToFile(newFile, json);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return newFile.exists();
        }

        return false;
    }

    public boolean has(String name, boolean activeFolder)
    {
        try
        {
            File parent = RCFileTypeRegistry.getDirectory(activeFolder);
            return parent != null && (new File(parent, name + "." + FILE_SUFFIX).exists());
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error when looking up inventory generation component", e);
        }

        return false;
    }

    public boolean delete(String name, boolean activeFolder)
    {
        try
        {
            File parent = RCFileTypeRegistry.getDirectory(activeFolder);
            return parent != null && (new File(parent, name + "." + FILE_SUFFIX).delete());
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error when deleting inventory generation component", e);
        }

        return false;
    }

    public Set<String> list(boolean activeFolder)
    {
        return StructureSaveHandler.listFiles(activeFolder, FileFilterUtils.suffixFileFilter(FILE_SUFFIX));
    }
}
