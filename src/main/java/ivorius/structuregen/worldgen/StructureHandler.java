package ivorius.structuregen.worldgen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ivorius.structuregen.StructureGen;
import ivorius.structuregen.json.NbtToJson;
import ivorius.structuregen.json.StringTypeAdapterFactory;
import ivorius.structuregen.worldgen.blockTransformers.BlockTransformer;
import ivorius.structuregen.worldgen.blockTransformers.BlockTransformerProvider;
import ivorius.structuregen.worldgen.genericStructures.BiomeGenerationInfo;
import ivorius.structuregen.worldgen.genericStructures.GenericStructureInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureHandler
{
    private static Map<String, StructureInfo> allStructures = new HashMap<>();
    private static Map<String, StructureInfo> generatingStructures = new HashMap<>();
    private static Map<String, StructureSelector> structureSelectorsInBiomes = new HashMap<>();

    private static StringTypeAdapterFactory<BlockTransformer> blockTransformerAdapterFactory;
    private static Map<String, BlockTransformerProvider> blockTransformerProviders = new HashMap<>();

    private static Gson gson = createGson();

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(GenericStructureInfo.class, new GenericStructureInfo.Serializer());
        builder.registerTypeAdapter(BiomeGenerationInfo.class, new BiomeGenerationInfo.Serializer());
        blockTransformerAdapterFactory = new StringTypeAdapterFactory<>("transformer", "type");
        builder.registerTypeHierarchyAdapter(BlockTransformer.class, blockTransformerAdapterFactory);

        NbtToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public static void registerStructure(StructureInfo info, String name, boolean generates)
    {
        if (info.areDependenciesResolved())
        {
            if (allStructures.containsKey(name))
            {
                StructureGen.logger.warn("Structure '" + name + "' already registered!");
            }
            else
            {
                allStructures.put(name, info);
                if (generates)
                {
                    generatingStructures.put(name, info);
                    structureSelectorsInBiomes.clear();
                }

                StructureGen.logger.info("Registered structure with structureTitle '" + name + "'");
            }
        }
    }

    public static void registerStructure(ResourceLocation resourceLocation, String name, boolean generates)
    {
        GenericStructureInfo structureInfo = StructureSaveHandler.structureInfoFromResource(resourceLocation);

        if (structureInfo != null)
        {
            registerStructure(structureInfo, name, generates);
        }
    }

    public static void registerStructures(String modID, boolean generating, String... names)
    {
        String path = generating ? "structures/genericStructures/" : "structures/silentStructures/";

        for (String name : names)
        {
            registerStructure(new ResourceLocation(modID, path + name + ".zip"), name, generating);
        }
    }

    public static StructureInfo getStructure(String name)
    {
        return allStructures.get(name);
    }

    public static void removeStructure(String name)
    {
        allStructures.remove(name);
        generatingStructures.remove(name);
        structureSelectorsInBiomes.clear();
    }

    public static GenericStructureInfo createStructureFromJSON(String jsonData)
    {
        return gson.fromJson(jsonData, GenericStructureInfo.class);
    }

    public static String createJSONFromStructure(GenericStructureInfo structureInfo)
    {
        return gson.toJson(structureInfo, GenericStructureInfo.class);
    }

    public static Collection<StructureInfo> getAllStructures()
    {
        return allStructures.values();
    }

    public static Collection<StructureInfo> getAllGeneratingStructures()
    {
        return generatingStructures.values();
    }

    public static Set<String> getAllStructureNames()
    {
        return allStructures.keySet();
    }

    public static StructureSelector getStructureSelectorInBiome(BiomeGenBase biome)
    {
        if (!structureSelectorsInBiomes.containsKey(biome.biomeName))
        {
            structureSelectorsInBiomes.put(biome.biomeName, new StructureSelector(getAllGeneratingStructures(), biome));
        }

        return structureSelectorsInBiomes.get(biome.biomeName);
    }

    public static StringTypeAdapterFactory<BlockTransformer> blockTransformerAdapterFactory()
    {
        return blockTransformerAdapterFactory;
    }

    public static <T extends BlockTransformer> void registerBlockTransformer(String id, Class<T> clazz, BlockTransformerProvider<T> provider)
    {
        blockTransformerAdapterFactory.register(id, clazz, provider.serializer(), provider.deserializer());
        blockTransformerProviders.put(id, provider);
    }

    public static Class<? extends BlockTransformer> blockTransformerTypeForID(String id)
    {
        return blockTransformerAdapterFactory.objectClass(id);
    }

    public static String blockTransformerIDForType(Class<? extends BlockTransformer> type)
    {
        return blockTransformerAdapterFactory.type(type);
    }

    public static Collection<String> allBlockTransformerIDs()
    {
        return blockTransformerAdapterFactory.allIDs();
    }

    public static BlockTransformerProvider blockTransformerProviderForID(String id)
    {
        return blockTransformerProviders.get(id);
    }
}
