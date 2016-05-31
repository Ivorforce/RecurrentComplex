/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.files;

import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.Collection;

/**
 * Created by lukas on 21.09.15.
 */
public class RCFileTypeRegistry extends FileTypeRegistry
{
    public static final String ACTIVE_DIR_NAME = "active";
    public static final String INACTIVE_DIR_NAME = "inactive";

    public static String getDirectoryName(boolean activeFolder)
    {
        return activeFolder ? ACTIVE_DIR_NAME : INACTIVE_DIR_NAME;
    }

    public static File getBaseDirectory()
    {
        return RecurrentComplex.proxy.getBaseFolderFile("structures");
    }

    public static File getDirectory(boolean activeFolder)
    {
        return RCFileHelper.getValidatedFolder(getBaseDirectory(), getDirectoryName(activeFolder), true);
    }

    public void reloadCustomFiles()
    {
        reloadCustomFiles(keySet());
    }

    public void reloadCustomFiles(Collection<String> suffices)
    {
        clearCustomFiles(suffices);

        File structuresFile = IvFileHelper.getValidatedFolder(getBaseDirectory());
        if (structuresFile != null)
        {
            tryLoadAll(suffices, structuresFile, RCFileTypeRegistry.ACTIVE_DIR_NAME, true, "", true, true);
            tryLoadAll(suffices, structuresFile, RCFileTypeRegistry.INACTIVE_DIR_NAME, true, "", false, true);

            // Legacy
            tryLoadAll(suffices, structuresFile, "genericStructures", false, "", true, true);
            tryLoadAll(suffices, structuresFile, "silentStructures", false, "", false, true);
            tryLoadAll(suffices, structuresFile, "inventoryGenerators", false, "", true, true);
        }
    }

    protected void tryLoadAll(Collection<String> suffices, File structuresFile, String activeDirName, boolean create, String domain, boolean active, boolean custom)
    {
        File validatedFolder = RCFileHelper.getValidatedFolder(structuresFile, activeDirName, create);
        if (validatedFolder != null)
            tryLoadAll(validatedFolder.toPath(), new FileLoadContext(domain, active, custom), suffices);
    }

    public void loadFilesFromMod(String modid)
    {
        loadFilesFromMod(modid, keySet());
    }

    public void loadFilesFromMod(String modid, Collection<String> suffices)
    {
        modid = modid.toLowerCase();

        tryLoadAll(new ResourceLocation(modid, "structures/" + RCFileTypeRegistry.ACTIVE_DIR_NAME), new FileLoadContext(modid, true, false));
        tryLoadAll(new ResourceLocation(modid, "structures/" + RCFileTypeRegistry.INACTIVE_DIR_NAME), new FileLoadContext(modid, false, false));

        // Legacy
        tryLoadAll(new ResourceLocation(modid, "structures/genericStructures"), new FileLoadContext(modid, true, false));
        tryLoadAll(new ResourceLocation(modid, "structures/silentStructures"), new FileLoadContext(modid, false, false));
        tryLoadAll(new ResourceLocation(modid, "structures/inventoryGenerators"), new FileLoadContext(modid, true, false));
    }
}
