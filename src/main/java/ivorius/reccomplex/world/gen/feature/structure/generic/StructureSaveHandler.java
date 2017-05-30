/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import ivorius.ivtoolkit.tools.IvFileHelper;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.loading.FileLoaderRegistry;
import ivorius.reccomplex.files.loading.RCFileSuffix;
import ivorius.reccomplex.files.saving.FileSaverAdapter;
import ivorius.reccomplex.json.NBTToJson;
import ivorius.reccomplex.utils.ByteArrays;
import ivorius.reccomplex.utils.zip.ZipFinder;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by lukas on 25.05.14.
 */
public class StructureSaveHandler
{
    public static final StructureSaveHandler INSTANCE = new StructureSaveHandler(RCFileSuffix.STRUCTURE, StructureRegistry.INSTANCE);

    public static final String STRUCTURE_INFO_JSON_FILENAME = "structure.json";
    public static final String WORLD_DATA_NBT_FILENAME = "worldData.nbt";

    public final Gson gson;

    public String suffix;
    public StructureRegistry registry;

    public StructureSaveHandler(String suffix, StructureRegistry registry)
    {
        gson = createGson();
        this.suffix = suffix;
        this.registry = registry;
    }

    protected static void addZipEntry(ZipOutputStream zip, String path, byte[] bytes) throws IOException
    {
        ZipEntry jsonEntry = new ZipEntry(path);
        zip.putNextEntry(jsonEntry);
        jsonEntry.setSize(bytes.length);
        zip.write(bytes);
        zip.closeEntry();
    }

    public Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(GenericStructure.class, new GenericStructure.Serializer());
        StructureRegistry.TRANSFORMERS.constructGson(builder);
        StructureRegistry.GENERATION_TYPES.constructGson(builder);
        builder.registerTypeAdapter(GenericVariableDomain.Variable.class, new GenericVariableDomain.Variable.Serializer());

        NBTToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public GenericStructure fromJSON(String jsonData, NBTTagCompound worldData) throws JsonSyntaxException
    {
        GenericStructure structure = gson.fromJson(jsonData, GenericStructure.class);
        structure.worldDataCompound = worldData;
        return structure;
    }

    public String toJSON(GenericStructure structureInfo)
    {
        return gson.toJson(structureInfo, GenericStructure.class);
    }

    public GenericStructure fromResource(ResourceLocation resourceLocation)
    {
        try (ZipInputStream zipInputStream = new ZipInputStream(IvFileHelper.inputStreamFromResourceLocation(resourceLocation)))
        {
            return fromZip(zipInputStream);
        }
        catch (Exception ex)
        {
            RecurrentComplex.logger.error("Could not read generic structure " + resourceLocation.toString(), ex);
        }

        return null;
    }

    public GenericStructure fromZip(ZipInputStream zipInputStream) throws IOException
    {
        ZipFinder finder = new ZipFinder();

        ZipFinder.Result<String> json = finder.bytes(STRUCTURE_INFO_JSON_FILENAME, String::new);
        ZipFinder.Result<NBTTagCompound> worldData = finder.bytes(WORLD_DATA_NBT_FILENAME,
                bytes -> CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes)));

        try
        {
            finder.read(zipInputStream);
            return fromJSON(json.get(), worldData.get());
        }
        catch (IOException | ZipFinder.MissingEntryException e)
        {
            throw new IOException("Error loading structure", e);
        }
    }

    public void toZip(Structure<?> structure, ZipOutputStream zipOutputStream) throws IOException
    {
        GenericStructure copy = structure.copyAsGenericStructure();
        Objects.requireNonNull(copy);

        addZipEntry(zipOutputStream, STRUCTURE_INFO_JSON_FILENAME, toJSON(copy).getBytes());
        addZipEntry(zipOutputStream, WORLD_DATA_NBT_FILENAME, ByteArrays.toByteArray(s -> CompressedStreamTools.writeCompressed(copy.worldDataCompound, s)));

        zipOutputStream.close();
    }

    public class Loader extends FileLoaderRegistry<GenericStructure>
    {
        public Loader()
        {
            super(StructureSaveHandler.this.suffix, StructureSaveHandler.this.registry);
        }

        @Override
        public GenericStructure read(Path path, String name) throws Exception
        {
            try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(path)))
            {
                return fromZip(zip);
            }
        }
    }

    public class Saver extends FileSaverAdapter<Structure<?>>
    {
        public Saver(String id)
        {
            super(id, StructureSaveHandler.this.suffix, StructureSaveHandler.this.registry);
        }

        @Override
        public void saveFile(Path path, Structure<?> structure) throws Exception
        {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(path)))
            {
                toZip(structure, zipOutputStream);
            }
        }
    }
}
