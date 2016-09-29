/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.RCFileTypeRegistry;
import ivorius.reccomplex.json.NbtToJson;
import ivorius.reccomplex.structures.generic.StructureSaveHandler;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Created by lukas on 25.05.14.
 */
public class ItemCollectionSaveHandler
{
    public static final ItemCollectionSaveHandler INSTANCE = new ItemCollectionSaveHandler();

    public static final String FILE_SUFFIX = "rcig";

    private Gson gson = createGson();

    public boolean save(@Nonnull Component info, @Nonnull String name, boolean active)
    {
        File parent = RCFileTypeRegistry.getDirectory(active);
        if (parent != null)
        {
            File newFile = new File(parent, String.format("%s.%s", name, FILE_SUFFIX));
            String json = toJSON(info);

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

    public Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(Component.class, new Component.Serializer());
        NbtToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public void write(ByteBuf data, Component component)
    {
        ByteBufUtils.writeUTF8String(data, toJSON(component));
    }

    @Nullable
    public Component read(ByteBuf data)
    {
        try
        {
            return this.fromJSON(ByteBufUtils.readUTF8String(data));
        }
        catch (InventoryLoadException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public String toJSON(Component inventoryGenerator)
    {
        return gson.toJson(inventoryGenerator, Component.class);
    }

    public Component fromJSON(String json) throws InventoryLoadException
    {
        try
        {
            return gson.fromJson(json, Component.class);
        }
        catch (JsonSyntaxException e)
        {
            throw new InventoryLoadException(e);
        }
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
