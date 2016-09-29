/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.files;

import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.util.Collection;

/**
 * Created by lukas on 21.09.15.
 */
public class RCFileTypeRegistry extends FileTypeRegistry
{
    public static final String ACTIVE_DIR_NAME = "active";
    public static final String INACTIVE_DIR_NAME = "inactive";

    public static final String RESOURCES_FILE_NAME = "structures";

    public static String getDirectoryName(boolean activeFolder)
    {
        return activeFolder ? ACTIVE_DIR_NAME : INACTIVE_DIR_NAME;
    }

    public static File getBaseDirectory()
    {
        return RecurrentComplex.proxy.getBaseFolderFile(RESOURCES_FILE_NAME);
    }

    public static File getDirectory(boolean activeFolder)
    {
        return RCFileHelper.getValidatedFolder(getBaseDirectory(), getDirectoryName(activeFolder), true);
    }

    public void loadModFiles()
    {
        LeveledRegistry.Level level = LeveledRegistry.Level.MODDED;

        clearFiles(level);
        for (String modid : Loader.instance().getIndexedModList().keySet())
            loadFilesFromDomain(modid, level, keySet());
    }

    public void loadCustomFiles()
    {
        loadCustomFiles(keySet());
    }

    public void loadCustomFiles(Collection<String> suffices)
    {
        LeveledRegistry.Level level = LeveledRegistry.Level.CUSTOM;

        clearFiles(level);

        File directory = IvFileHelper.getValidatedFolder(getBaseDirectory());
        if (directory != null)
            loadFilesFromDirectory(directory, level, suffices);
    }

    public void loadFilesFromDirectory(File directory, LeveledRegistry.Level level, Collection<String> suffices)
    {
        tryLoadAll(directory, RCFileTypeRegistry.ACTIVE_DIR_NAME, true, "", true, level, suffices);
        tryLoadAll(directory, RCFileTypeRegistry.INACTIVE_DIR_NAME, true, "", false, level, suffices);

        // Legacy
        tryLoadAll(directory, "genericStructures", false, "", true, level, suffices);
        tryLoadAll(directory, "silentStructures", false, "", false, level, suffices);
        tryLoadAll(directory, "inventoryGenerators", false, "", true, level, suffices);
    }

    protected void tryLoadAll(File structuresFile, String activeDirName, boolean create, String domain, boolean active, LeveledRegistry.Level level, Collection<String> suffices)
    {
        File validatedFolder = RCFileHelper.getValidatedFolder(structuresFile, activeDirName, create);
        if (validatedFolder != null)
            tryLoadAll(validatedFolder.toPath(), new FileLoadContext(domain, active, level), suffices);
    }

    public void loadFilesFromDomain(String domain, LeveledRegistry.Level level, Collection<String> suffices)
    {
        domain = domain.toLowerCase();

        tryLoadAll(new ResourceLocation(domain, String.format("%s/%s", RESOURCES_FILE_NAME, RCFileTypeRegistry.ACTIVE_DIR_NAME)), new FileLoadContext(domain, true, level), suffices);
        tryLoadAll(new ResourceLocation(domain, String.format("%s/%s", RESOURCES_FILE_NAME, RCFileTypeRegistry.INACTIVE_DIR_NAME)), new FileLoadContext(domain, false, level), suffices);

        // Legacy
        tryLoadAll(new ResourceLocation(domain, "structures/genericStructures"), new FileLoadContext(domain, true, level), suffices);
        tryLoadAll(new ResourceLocation(domain, "structures/silentStructures"), new FileLoadContext(domain, false, level), suffices);
        tryLoadAll(new ResourceLocation(domain, "structures/inventoryGenerators"), new FileLoadContext(domain, true, level), suffices);
    }
}
