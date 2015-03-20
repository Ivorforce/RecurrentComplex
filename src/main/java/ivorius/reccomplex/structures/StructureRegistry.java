/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import cpw.mods.fml.common.eventhandler.Event;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.events.RCEventBus;
import ivorius.reccomplex.events.StructureRegistrationEvent;
import ivorius.reccomplex.json.NbtToJson;
import ivorius.reccomplex.json.SerializableStringTypeRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.structures.generic.StructureSaveHandler;
import ivorius.reccomplex.structures.generic.transformers.Transformer;
import ivorius.reccomplex.structures.generic.gentypes.MazeGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.StaticGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.StructureListGenerationInfo;
import ivorius.reccomplex.worldgen.StructureSelector;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by lukas on 24.05.14.
 */
public class
        StructureRegistry
{
    private static BiMap<String, StructureInfo> allStructures = HashBiMap.create();
    private static Map<String, StructureInfo> generatingStructures = new HashMap<>();

    private static Map<Class<StructureGenerationInfo>, List<Pair<StructureInfo, StructureGenerationInfo>>> cachedGeneration = new HashMap<>();

    private static Map<Pair<Integer, String>, StructureSelector> structureSelectors = new HashMap<>();

    private static SerializableStringTypeRegistry<Transformer> transformerRegistry = new SerializableStringTypeRegistry<>("transformer", "type", Transformer.class);
    private static SerializableStringTypeRegistry<StructureGenerationInfo> structureGenerationInfoRegistry = new SerializableStringTypeRegistry<>("generationInfo", "type", StructureGenerationInfo.class);

    private static Gson gson = createGson();

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(GenericStructureInfo.class, new GenericStructureInfo.Serializer());
        transformerRegistry.constructGson(builder);
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

    public static <T extends StructureGenerationInfo> Collection<Pair<StructureInfo, T>> getStructureGenerations(Class<T> clazz)
    {
        Map cachedGeneration = StructureRegistry.cachedGeneration;

        List<Pair<StructureInfo, T>> pairs = (List<Pair<StructureInfo, T>>) cachedGeneration.get(clazz);
        if (pairs != null)
            return pairs;

        ArrayList<Pair<StructureInfo, T>> pairsArrayList = new ArrayList<>();
        for (StructureInfo info : getAllGeneratingStructures())
        {
            for (T t : info.generationInfos(clazz))
                pairsArrayList.add(Pair.of(info, t));
        }
        pairsArrayList.trimToSize();
        cachedGeneration.put(clazz, pairsArrayList);

        return pairsArrayList;
    }

    public static <T extends StructureGenerationInfo> Collection<Pair<StructureInfo, T>> getStructureGenerations(Class<T> clazz, final Predicate<Pair<StructureInfo, T>> predicate)
    {
        return Collections2.filter(getStructureGenerations(clazz), predicate);
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

    public static Collection<Pair<StructureInfo, StructureListGenerationInfo>> getStructuresInList(final String listID, final ForgeDirection front)
    {
        return getStructureGenerations(StructureListGenerationInfo.class, new Predicate<Pair<StructureInfo, StructureListGenerationInfo>>()
        {
            @Override
            public boolean apply(Pair<StructureInfo, StructureListGenerationInfo> input)
            {
                return listID.equals(input.getRight().listID)
                        && (front == null || input.getLeft().isRotatable() || input.getRight().front == front);
            }
        });
    }

    public static Collection<Pair<StructureInfo, MazeGenerationInfo>> getStructuresInMaze(final String mazeID)
    {
        return getStructureGenerations(MazeGenerationInfo.class, new Predicate<Pair<StructureInfo, MazeGenerationInfo>>()
        {
            @Override
            public boolean apply(Pair<StructureInfo, MazeGenerationInfo> input)
            {
                MazeGenerationInfo info = input.getRight();
                return mazeID.equals(info.mazeID) && info.mazeComponent.isValid();
            }
        });
    }

    private static boolean chunkContains(int chunkX, int chunkZ, int x, int z)
    {
        return (x >> 4) == chunkX && (z >> 4) == chunkZ;
    }

    public static Collection<Pair<StructureInfo, StaticGenerationInfo>> getStaticStructuresAt(final int chunkX, final int chunkZ, final World world, final ChunkCoordinates spawnPos)
    {
        return getStructureGenerations(StaticGenerationInfo.class, new Predicate<Pair<StructureInfo, StaticGenerationInfo>>()
        {
            @Override
            public boolean apply(@Nullable Pair<StructureInfo, StaticGenerationInfo> input)
            {
                StaticGenerationInfo info = input.getRight();
                return info.dimensionMatcher.apply(world.provider)
                        && chunkContains(chunkX, chunkZ, info.getPositionX(spawnPos), info.getPositionZ(spawnPos)
                );
            }
        });
    }

    public static SerializableStringTypeRegistry<Transformer> getTransformerRegistry()
    {
        return transformerRegistry;
    }

    public static SerializableStringTypeRegistry<StructureGenerationInfo> getStructureGenerationInfoRegistry()
    {
        return structureGenerationInfoRegistry;
    }

    private static void clearCaches()
    {
        structureSelectors.clear();
        cachedGeneration.clear();
    }
}
