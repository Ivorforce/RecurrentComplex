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
import cpw.mods.fml.common.eventhandler.Event;
import ivorius.ivtoolkit.maze.MazePath;
import ivorius.ivtoolkit.maze.MazeRoom;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.events.RCEventBus;
import ivorius.reccomplex.events.StructureRegistrationEvent;
import ivorius.reccomplex.json.NbtToJson;
import ivorius.reccomplex.json.StringTypeAdapterFactory;
import ivorius.reccomplex.worldgen.blockTransformers.BlockTransformer;
import ivorius.reccomplex.worldgen.blockTransformers.BlockTransformerProvider;
import ivorius.reccomplex.worldgen.genericStructures.*;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.MazeGenerationInfo;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.VanillaStructureSpawnInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureRegistry
{
    private static BiMap<String, StructureInfo> allStructures = HashBiMap.create();
    private static Map<String, StructureInfo> generatingStructures = new HashMap<>();

    private static Map<Pair<Integer, String>, StructureSelector> structureSelectors = new HashMap<>();
    private static Map<String, List<StructureInfo>> structuresInMazes = new HashMap<>();

    private static StringTypeAdapterFactory<BlockTransformer> blockTransformerAdapterFactory;
    private static Map<String, BlockTransformerProvider> blockTransformerProviders = new HashMap<>();

    private static Gson gson = createGson();

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(GenericStructureInfo.class, new GenericStructureInfo.Serializer());
        builder.registerTypeAdapter(BiomeGenerationInfo.class, new BiomeGenerationInfo.Serializer());
        builder.registerTypeAdapter(DimensionGenerationInfo.class, new DimensionGenerationInfo.Serializer());
        builder.registerTypeAdapter(NaturalGenerationInfo.class, new NaturalGenerationInfo.Serializer());
        builder.registerTypeAdapter(MazeGenerationInfo.class, new MazeGenerationInfo.Serializer());
        builder.registerTypeAdapter(VanillaStructureSpawnInfo.class, new VanillaStructureSpawnInfo.Serializer());
        builder.registerTypeAdapter(SavedMazeComponent.class, new SavedMazeComponent.Serializer());
        builder.registerTypeAdapter(MazeRoom.class, new SavedMazeComponent.RoomSerializer());
        builder.registerTypeAdapter(MazePath.class, new SavedMazeComponent.PathSerializer());
        blockTransformerAdapterFactory = new StringTypeAdapterFactory<>("transformer", "type");
        builder.registerTypeHierarchyAdapter(BlockTransformer.class, blockTransformerAdapterFactory);

        NbtToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public static void registerStructure(StructureInfo info, String key, boolean generates)
    {
        if (info.areDependenciesResolved())
        {
            StructureRegistrationEvent.Pre event = new StructureRegistrationEvent.Pre(key, info, generates);
            RCEventBus.INSTANCE.post(event);

            if (event.getResult() != Event.Result.DENY)
            {
                generates = event.shouldGenerate && !RCConfig.isStructureDisabled(key);

                String baseString = allStructures.containsKey(key) ? "Overwrote structure '%s'%s" : "Registered structure '%s'%s";
                String genPart = generates ? " (Generating)" : "";
                RecurrentComplex.logger.info(String.format(baseString, key, genPart));

                allStructures.put(key, info);
                if (generates)
                    generatingStructures.put(key, info);
                else
                    generatingStructures.remove(key); // Make sure to honour the new 'generates' boolean

                clearCaches();

                RCEventBus.INSTANCE.post(new StructureRegistrationEvent.Post(key, info, generates));
            }
        }
    }

    public static boolean registerStructure(ResourceLocation resourceLocation, String key, boolean generates)
    {
        GenericStructureInfo structureInfo = StructureSaveHandler.structureInfoFromResource(resourceLocation);

        if (structureInfo != null)
        {
            registerStructure(structureInfo, key, generates);
            return true;
        }
        else
            return false;
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

    public static StructureSelector getStructureSelector(BiomeGenBase biome, WorldProvider provider)
    {
        Pair<Integer, String> pair = new ImmutablePair<>(provider.dimensionId, biome.biomeName);
        StructureSelector structureSelector = structureSelectors.get(pair);

        if (structureSelector == null || !structureSelector.isValid(biome, provider))
        {
            structureSelector = new StructureSelector(getAllGeneratingStructures(), biome, provider);
            structureSelectors.put(pair, structureSelector);
        }

        return structureSelector;
    }

    public static List<StructureInfo> getStructuresInMaze(String mazeID)
    {
        if (!structuresInMazes.containsKey(mazeID))
        {
            List<StructureInfo> structureInfos = new ArrayList<>();
            for (StructureInfo info : getAllGeneratingStructures())
            {
                if (mazeID.equals(info.mazeID()) && info.mazeComponent().isValid())
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
        structureSelectors.clear();
        structuresInMazes.clear();
    }
}
