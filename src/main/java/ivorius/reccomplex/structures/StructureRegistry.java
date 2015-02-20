/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

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
import ivorius.reccomplex.json.SerializableStringTypeRegistry;
import ivorius.reccomplex.structures.generic.*;
import ivorius.reccomplex.structures.generic.blocktransformers.BlockTransformer;
import ivorius.reccomplex.structures.generic.gentypes.MazeGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.VanillaStructureSpawnInfo;
import ivorius.reccomplex.worldgen.StructureSelector;
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
    private static Map<String, List<Pair<StructureInfo, MazeGenerationInfo>>> structuresInMazes = new HashMap<>();

    private static SerializableStringTypeRegistry<BlockTransformer> blockTransformerRegistry = new SerializableStringTypeRegistry<>("transformer", "type", BlockTransformer.class);
    private static SerializableStringTypeRegistry<StructureGenerationInfo> structureGenerationInfoRegistry = new SerializableStringTypeRegistry<>("generationInfo", "type", StructureGenerationInfo.class);

    private static Gson gson = createGson();

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(GenericStructureInfo.class, new GenericStructureInfo.Serializer());
        blockTransformerRegistry.constructGson(builder);
        structureGenerationInfoRegistry.constructGson(builder);

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

    public static List<Pair<StructureInfo, MazeGenerationInfo>> getStructuresInMaze(String mazeID)
    {
        if (!structuresInMazes.containsKey(mazeID))
        {
            List<Pair<StructureInfo, MazeGenerationInfo>> structureInfos = new ArrayList<>();
            for (StructureInfo info : getAllGeneratingStructures())
            {
                for (MazeGenerationInfo mazeGenerationInfo : info.generationInfos(MazeGenerationInfo.class))
                {
                    if (mazeID.equals(mazeGenerationInfo.mazeID) && mazeGenerationInfo.mazeComponent.isValid())
                        structureInfos.add(Pair.of(info, mazeGenerationInfo));
                }
            }
            structuresInMazes.put(mazeID, structureInfos);
        }

        return structuresInMazes.get(mazeID);
    }

    public static SerializableStringTypeRegistry<BlockTransformer> getBlockTransformerRegistry()
    {
        return blockTransformerRegistry;
    }

    public static SerializableStringTypeRegistry<StructureGenerationInfo> getStructureGenerationInfoRegistry()
    {
        return structureGenerationInfoRegistry;
    }

    private static void clearCaches()
    {
        structureSelectors.clear();
        structuresInMazes.clear();
    }
}
