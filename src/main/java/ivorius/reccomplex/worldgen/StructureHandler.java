/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import ivorius.ivtoolkit.maze.MazePath;
import ivorius.ivtoolkit.maze.MazeRoom;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.json.NbtToJson;
import ivorius.reccomplex.json.StringTypeAdapterFactory;
import ivorius.reccomplex.worldgen.blockTransformers.BlockTransformer;
import ivorius.reccomplex.worldgen.blockTransformers.BlockTransformerProvider;
import ivorius.reccomplex.worldgen.genericStructures.BiomeGenerationInfo;
import ivorius.reccomplex.worldgen.genericStructures.GenericStructureInfo;
import ivorius.reccomplex.worldgen.genericStructures.MazeComponent;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.MazeGenerationInfo;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.NaturalGenerationInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.*;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureHandler
{
    private static BiMap<String, StructureInfo> allStructures = HashBiMap.create();
    private static Map<String, StructureInfo> generatingStructures = new HashMap<>();

    private static Map<String, StructureSelector> structureSelectorsInBiomes = new HashMap<>();
    private static Map<String, List<StructureInfo>> structuresInMazes = new HashMap<>();

    private static StringTypeAdapterFactory<BlockTransformer> blockTransformerAdapterFactory;
    private static Map<String, BlockTransformerProvider> blockTransformerProviders = new HashMap<>();

    private static Gson gson = createGson();

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(GenericStructureInfo.class, new GenericStructureInfo.Serializer());
        builder.registerTypeAdapter(BiomeGenerationInfo.class, new BiomeGenerationInfo.Serializer());
        builder.registerTypeAdapter(NaturalGenerationInfo.class, new NaturalGenerationInfo.Serializer());
        builder.registerTypeAdapter(MazeGenerationInfo.class, new MazeGenerationInfo.Serializer());
        builder.registerTypeAdapter(MazeComponent.class, new MazeComponent.Serializer());
        builder.registerTypeAdapter(MazeRoom.class, new MazeComponent.RoomSerializer());
        builder.registerTypeAdapter(MazePath.class, new MazeComponent.PathSerializer());
        blockTransformerAdapterFactory = new StringTypeAdapterFactory<>("transformer", "type");
        builder.registerTypeHierarchyAdapter(BlockTransformer.class, blockTransformerAdapterFactory);

        NbtToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public static void registerStructure(StructureInfo info, String key, boolean generates)
    {
        if (info.areDependenciesResolved())
        {
            generates = generates && !RCConfig.isStructureDisabled(key);

            String baseString = allStructures.containsKey(key) ? "Overwrote structure '%s'%s" : "Registered structure '%s'%s";
            String genPart = generates ? " (Generating)" : "";
            RecurrentComplex.logger.info(String.format(baseString, key, genPart));

            allStructures.put(key, info);
            if (generates)
                generatingStructures.put(key, info);
            else
                generatingStructures.remove(key); // Make sure to honour the new 'generates' boolean

            clearCaches();
        }
    }

    public static void registerStructure(ResourceLocation resourceLocation, String key, boolean generates)
    {
        GenericStructureInfo structureInfo = StructureSaveHandler.structureInfoFromResource(resourceLocation);

        if (structureInfo != null)
        {
            registerStructure(structureInfo, key, generates);
        }
    }

    public static StructureInfo getStructure(String key)
    {
        return allStructures.get(key);
    }

    public static String getName(StructureInfo structureInfo)
    {
        return allStructures.inverse().get(structureInfo);
    }

    public static void removeStructure(String key)
    {
        allStructures.remove(key);
        generatingStructures.remove(key);
        clearCaches();
    }

    public static GenericStructureInfo createStructureFromJSON(String jsonData) throws JsonSyntaxException
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
            structureSelectorsInBiomes.put(biome.biomeName, new StructureSelector(getAllGeneratingStructures(), biome));

        return structureSelectorsInBiomes.get(biome.biomeName);
    }

    public static List<StructureInfo> getStructuresInMaze(String mazeID)
    {
        if (!structuresInMazes.containsKey(mazeID))
        {
            List<StructureInfo> structureInfos = new ArrayList<>();
            for (StructureInfo info : getAllGeneratingStructures())
            {
                if (mazeID.equals(info.mazeID()))
                    structureInfos.add(info);
            }
            structuresInMazes.put(mazeID, structureInfos);
        }

        return structuresInMazes.get(mazeID);
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

    private static void clearCaches()
    {
        structureSelectorsInBiomes.clear();
        structuresInMazes.clear();
    }
}
