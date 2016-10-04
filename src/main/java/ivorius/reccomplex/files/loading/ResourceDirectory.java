/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files.loading;

import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.RCFiles;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Created by lukas on 03.10.16.
 */
public enum ResourceDirectory
{
    ACTIVE(true, LeveledRegistry.Level.CUSTOM), INACTIVE(false, LeveledRegistry.Level.CUSTOM),
    SERVER_ACTIVE(true, LeveledRegistry.Level.SERVER), SERVER_INACTIVE(false, LeveledRegistry.Level.SERVER);

    public static final String ACTIVE_DIR_NAME = "active";
    public static final String INACTIVE_DIR_NAME = "inactive";
    public static final String RESOURCES_FILE_NAME = "structures";

    private boolean active;
    private LeveledRegistry.Level level;

    ResourceDirectory(boolean active, LeveledRegistry.Level level)
    {
        this.active = active;
        this.level = level;
    }

    public static ResourceDirectory custom(boolean active)
    {
        return active ? ACTIVE : INACTIVE;
    }

    public static ResourceDirectory server(boolean active)
    {
        return active ? SERVER_ACTIVE : SERVER_INACTIVE;
    }

    public static ResourceDirectory read(ByteBuf buf)
    {
        return valueOf(ByteBufUtils.readUTF8String(buf));
    }

    public static File getCustomDirectory()
    {
        return RecurrentComplex.proxy.getBaseFolderFile(RESOURCES_FILE_NAME);
    }

    @Nonnull
    public static File getSaveDirectory()
    {
        return RCFiles.getValidatedFolder(getServer().getEntityWorld().getSaveHandler().getWorldDirectory(), RESOURCES_FILE_NAME, true);
    }

    public static void reloadModFiles(FileLoader loader)
    {
        LeveledRegistry.Level level = LeveledRegistry.Level.MODDED;

        loader.clearFiles(level);
        for (String modid : Loader.instance().getIndexedModList().keySet())
            loadFilesFromDomain(loader, modid, level, loader.keySet());
    }

    public static void reloadCustomFiles(FileLoader loader)
    {
        reloadFilesFromDirectory(loader, LeveledRegistry.Level.CUSTOM, getCustomDirectory());
    }

    public static void reloadServerFiles(FileLoader loader)
    {
        reloadFilesFromDirectory(loader, LeveledRegistry.Level.SERVER, getSaveDirectory());
    }

    public static void reloadFilesFromDirectory(FileLoader loader, LeveledRegistry.Level level, File directory)
    {
        loader.clearFiles(level);
        loadFilesFromDirectory(loader, directory, level, loader.keySet());
    }

    public static void loadFilesFromDirectory(FileLoader loader, File directory, LeveledRegistry.Level level, Collection<String> suffices)
    {
        tryLoadAll(loader, directory, ACTIVE.subDirectoryName(), true, "", true, level, suffices);
        tryLoadAll(loader, directory, INACTIVE.subDirectoryName(), true, "", false, level, suffices);

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

        loader.tryLoadAll(resourceLocation(domain, ACTIVE_DIR_NAME), new FileLoadContext(domain, true, level), suffices);
        loader.tryLoadAll(resourceLocation(domain, INACTIVE_DIR_NAME), new FileLoadContext(domain, false, level), suffices);

        // Legacy
        loader.tryLoadAll(new ResourceLocation(domain, "structures/genericStructures"), new FileLoadContext(domain, true, level), suffices);
        loader.tryLoadAll(new ResourceLocation(domain, "structures/silentStructures"), new FileLoadContext(domain, false, level), suffices);
        loader.tryLoadAll(new ResourceLocation(domain, "structures/inventoryGenerators"), new FileLoadContext(domain, true, level), suffices);
    }

    @Nonnull
    protected static ResourceLocation resourceLocation(String domain, String directoryName)
    {
        return new ResourceLocation(domain, String.format("%s/%s", RESOURCES_FILE_NAME, directoryName));
    }

    protected static MinecraftServer getServer()
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            throw new IllegalStateException();

        MinecraftServer instance = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (instance == null)
            throw new IllegalStateException();

        return instance;
    }

    public boolean isServer()
    {
        return this == SERVER_ACTIVE || this == SERVER_INACTIVE;
    }

    public boolean isCustom()
    {
        return this == ACTIVE || this == INACTIVE;
    }

    public LeveledRegistry.Level getLevel()
    {
        return level;
    }

    @Nonnull
    public Path toPath()
    {
        return toFile().toPath();
    }

    public File toFile()
    {
        return RCFiles.getValidatedFolder(isServer() ? getSaveDirectory() : getCustomDirectory(), subDirectoryName(), true);
    }

    public String subDirectoryName()
    {
        return isActive() ? ACTIVE_DIR_NAME : INACTIVE_DIR_NAME;
    }

    public boolean isActive()
    {
        return active;
    }

    public ResourceDirectory opposite()
    {
        switch (this)
        {
            case ACTIVE:
                return INACTIVE;
            case INACTIVE:
                return ACTIVE;
            case SERVER_ACTIVE:
                return SERVER_INACTIVE;
            case SERVER_INACTIVE:
                return SERVER_ACTIVE;
            default:
                throw new IllegalStateException();
        }
    }

    public void write(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, name());
    }
}
