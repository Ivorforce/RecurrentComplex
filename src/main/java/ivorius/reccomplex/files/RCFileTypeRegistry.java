/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.files;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 21.09.15.
 */
public class RCFileTypeRegistry extends FileTypeRegistry
{
    public static final String ACTIVE_DIR_NAME = "active";
    public static final String INACTIVE_DIR_NAME = "inactive";

    public static final String RESOURCES_FILE_NAME = "structures";

    public static File getBaseDirectory()
    {
        return RecurrentComplex.proxy.getBaseFolderFile(RESOURCES_FILE_NAME);
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
        tryLoadAll(directory, Directory.ACTIVE.directoryName(), true, "", true, level, suffices);
        tryLoadAll(directory, Directory.INACTIVE.directoryName(), true, "", false, level, suffices);

        // Legacy
        tryLoadAll(directory, "genericStructures", false, "", true, level, suffices);
        tryLoadAll(directory, "silentStructures", false, "", false, level, suffices);
        tryLoadAll(directory, "inventoryGenerators", false, "", true, level, suffices);
    }

    protected void tryLoadAll(File structuresFile, String directoryName, boolean create, String domain, boolean active, LeveledRegistry.Level level, Collection<String> suffices)
    {
        File validatedFolder = RCFileHelper.getValidatedFolder(structuresFile, directoryName, create);
        if (validatedFolder != null)
            tryLoadAll(validatedFolder.toPath(), new FileLoadContext(domain, active, level), suffices);
    }

    public void loadFilesFromDomain(String domain, LeveledRegistry.Level level, Collection<String> suffices)
    {
        domain = domain.toLowerCase();

        tryLoadAll(Directory.ACTIVE.toResourceLocation(domain), new FileLoadContext(domain, true, level), suffices);
        tryLoadAll(Directory.INACTIVE.toResourceLocation(domain), new FileLoadContext(domain, false, level), suffices);

        // Legacy
        tryLoadAll(new ResourceLocation(domain, "structures/genericStructures"), new FileLoadContext(domain, true, level), suffices);
        tryLoadAll(new ResourceLocation(domain, "structures/silentStructures"), new FileLoadContext(domain, false, level), suffices);
        tryLoadAll(new ResourceLocation(domain, "structures/inventoryGenerators"), new FileLoadContext(domain, true, level), suffices);
    }

    public boolean tryWrite(Directory directory, String suffix, String name)
    {
        Path path = FileUtils.getFile(directory.toFile(), String.format("%s.%s", name, suffix)).toPath();
        return tryWrite(path, name);
    }

    public void write(Directory directory, String suffix, String name) throws Exception
    {
        Path path = FileUtils.getFile(directory.toFile(), String.format("%s.%s", name, suffix)).toPath();
        write(path, name);
    }

    public List<Path> tryDelete(Directory directory, String name, String suffix)
    {
        return tryDelete(directory.toPath(), name, suffix);
    }

    public Set<String> tryFindIDs(Directory directory, String suffix)
    {
        return tryFindIDs(directory.toPath(), suffix);
    }

    public enum Directory
    {
        ACTIVE, INACTIVE;

        public static Directory fromActive(boolean active)
        {
            return active ? ACTIVE : INACTIVE;
        }

        public static Directory read(ByteBuf buf)
        {
            return fromActive(buf.readBoolean());
        }

        @Nonnull
        public Path toPath()
        {
            return toFile().toPath();
        }

        public File toFile()
        {
            return RCFileHelper.getValidatedFolder(getBaseDirectory(), directoryName(), true);
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

        public Directory opposite()
        {
            return this == ACTIVE ? INACTIVE : ACTIVE;
        }

        public void write(ByteBuf buf)
        {
            buf.writeBoolean(isActive());
        }
    }
}
