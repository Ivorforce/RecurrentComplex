/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files.loading;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.RCFiles;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Created by lukas on 03.10.16.
 */
public enum ResourceDirectory
{
    ACTIVE, INACTIVE;

    public static final String ACTIVE_DIR_NAME = "active";
    public static final String INACTIVE_DIR_NAME = "inactive";
    public static final String RESOURCES_FILE_NAME = "structures";

    public static ResourceDirectory fromActive(boolean active)
    {
        return active ? ACTIVE : INACTIVE;
    }

    public static ResourceDirectory read(ByteBuf buf)
    {
        return fromActive(buf.readBoolean());
    }

    public static File getBaseDirectory()
    {
        return RecurrentComplex.proxy.getBaseFolderFile(RESOURCES_FILE_NAME);
    }

    public static void loadModFiles(FileLoader loader)
    {
        LeveledRegistry.Level level = LeveledRegistry.Level.MODDED;

        loader.clearFiles(level);
        for (String modid : Loader.instance().getIndexedModList().keySet())
            loadFilesFromDomain(loader, modid, level, loader.keySet());
    }

    public static void loadCustomFiles(FileLoader loader)
    {
        loadCustomFiles(loader, loader.keySet());
    }

    public static void loadCustomFiles(FileLoader loader, Collection<String> suffices)
    {
        LeveledRegistry.Level level = LeveledRegistry.Level.CUSTOM;

        loader.clearFiles(level);

        File directory = IvFileHelper.getValidatedFolder(getBaseDirectory());
        if (directory != null)
            loadFilesFromDirectory(loader, directory, level, suffices);
    }

    public static void loadFilesFromDirectory(FileLoader loader, File directory, LeveledRegistry.Level level, Collection<String> suffices)
    {
        tryLoadAll(loader, directory, ACTIVE.directoryName(), true, "", true, level, suffices);
        tryLoadAll(loader, directory, INACTIVE.directoryName(), true, "", false, level, suffices);

        // Legacy
        tryLoadAll(loader, directory, "genericStructures", false, "", true, level, suffices);
        tryLoadAll(loader, directory, "silentStructures", false, "", false, level, suffices);
        tryLoadAll(loader, directory, "inventoryGenerators", false, "", true, level, suffices);
    }

    protected static void tryLoadAll(FileLoader loader, File structuresFile, String directoryName, boolean create, String domain, boolean active, LeveledRegistry.Level level, Collection<String> suffices)
    {
        File validatedFolder = RCFiles.getValidatedFolder(structuresFile, directoryName, create);
        if (validatedFolder != null)
            loader.tryLoadAll(validatedFolder.toPath(), new FileLoadContext(domain, active, level), suffices);
    }

    public static void loadFilesFromDomain(FileLoader loader, String domain, LeveledRegistry.Level level, Collection<String> suffices)
    {
        domain = domain.toLowerCase();

        loader.tryLoadAll(ACTIVE.toResourceLocation(domain), new FileLoadContext(domain, true, level), suffices);
        loader.tryLoadAll(INACTIVE.toResourceLocation(domain), new FileLoadContext(domain, false, level), suffices);

        // Legacy
        loader.tryLoadAll(new ResourceLocation(domain, "structures/genericStructures"), new FileLoadContext(domain, true, level), suffices);
        loader.tryLoadAll(new ResourceLocation(domain, "structures/silentStructures"), new FileLoadContext(domain, false, level), suffices);
        loader.tryLoadAll(new ResourceLocation(domain, "structures/inventoryGenerators"), new FileLoadContext(domain, true, level), suffices);
    }

    @Nonnull
    public Path toPath()
    {
        return toFile().toPath();
    }

    public File toFile()
    {
        return RCFiles.getValidatedFolder(getBaseDirectory(), directoryName(), true);
    }

    public ResourceLocation toResourceLocation(String domain)
    {
        return new ResourceLocation(domain, String.format("%s/%s", RESOURCES_FILE_NAME, directoryName()));
    }

    public String directoryName()
    {
        return this == ACTIVE ? ACTIVE_DIR_NAME : INACTIVE_DIR_NAME;
    }

    public boolean isActive()
    {
        return this == ACTIVE;
    }

    public ResourceDirectory opposite()
    {
        return this == ACTIVE ? INACTIVE : ACTIVE;
    }

    public void write(ByteBuf buf)
    {
        buf.writeBoolean(isActive());
    }
}
