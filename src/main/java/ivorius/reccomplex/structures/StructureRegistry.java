/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import ivorius.reccomplex.structures.generic.gentypes.*;
import ivorius.reccomplex.structures.generic.transformers.Transformer;
import ivorius.reccomplex.utils.CustomizableBiMap;
import ivorius.reccomplex.utils.CustomizableMap;
import ivorius.reccomplex.worldgen.StructureSelector;
import ivorius.reccomplex.worldgen.villages.GenericVillageCreationHandler;
import ivorius.reccomplex.worldgen.villages.TemporaryVillagerRegistry;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureRegistry
{
    private static SerializableStringTypeRegistry<Transformer> transformerRegistry = new SerializableStringTypeRegistry<>("transformer", "type", Transformer.class);
    private static SerializableStringTypeRegistry<StructureGenerationInfo> structureGenerationInfoRegistry = new SerializableStringTypeRegistry<>("generationInfo", "type", StructureGenerationInfo.class);

    public static final StructureRegistry INSTANCE = new StructureRegistry();

    private CustomizableBiMap<String, StructureInfo> allStructures = new CustomizableBiMap<>();
    private CustomizableMap<String, StructureData> structureData = new CustomizableMap<>();

    private boolean needsGenerationCacheUpdate = true;
    private Set<String> generatingStructures = new HashSet<>();

    private Map<Class<? extends StructureGenerationInfo>, Collection<Pair<StructureInfo, ? extends StructureGenerationInfo>>> cachedGeneration = new HashMap<>();

    private Map<Pair<Integer, String>, StructureSelector> structureSelectors = new HashMap<>();

    private Gson gson = createGson();

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(GenericStructureInfo.class, new GenericStructureInfo.Serializer());
        transformerRegistry.constructGson(builder);
        structureGenerationInfoRegistry.constructGson(builder);

        NbtToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public boolean registerStructure(StructureInfo info, String key, String domain, boolean generates, boolean custom)
    {
        StructureRegistrationEvent.Pre event = new StructureRegistrationEvent.Pre(key, info, generates);
        RCEventBus.INSTANCE.post(event);

        if (event.getResult() != Event.Result.DENY && RCConfig.shouldStructureLoad(key, domain))
        {
            String baseString = allStructures.put(key, info, custom) != null ? "Replaced structure '%s'" : "Registered structure '%s'";
            RecurrentComplex.logger.info(String.format(baseString, key));

            structureData.put(key, new StructureData(!event.shouldGenerate, domain), custom);

            clearCaches();

            RCEventBus.INSTANCE.post(new StructureRegistrationEvent.Post(key, info, generates));

            return true;
        }

        return false;
    }

    public boolean hasStructure(String key)
    {
        return allStructures.getMap().containsKey(key);
    }

    public StructureInfo getStructure(String key)
    {
        return allStructures.getMap().get(key);
    }

    @Deprecated
    public String getName(StructureInfo structureInfo)
    {
        return structureID(structureInfo);
    }

    public String structureID(StructureInfo structureInfo)
    {
        return allStructures.getMap().inverse().get(structureInfo);
    }

    public void clearCustom()
    {
        structureData.clearCustom();
        allStructures.clearCustom();
    }

    public void unregisterStructure(String key, boolean custom)
    {
        StructureInfo info = allStructures.remove(key, custom);
        structureData.remove(key, custom);

        if (info != null)
            generatingStructures.remove(key);

        clearCaches();
    }

    public GenericStructureInfo createStructureFromJSON(String jsonData) throws JsonSyntaxException
    {
        return gson.fromJson(jsonData, GenericStructureInfo.class);
    }

    public String createJSONFromStructure(GenericStructureInfo structureInfo)
    {
        return gson.toJson(structureInfo, GenericStructureInfo.class);
    }

    public Set<StructureInfo> getAllStructures()
    {
        return Collections.unmodifiableSet(allStructures.getMap().values());
    }

    private void ensureGenerationCache()
    {
        if (needsGenerationCacheUpdate)
        {
            needsGenerationCacheUpdate = false;
            generatingStructures.clear();

            for (Map.Entry<String, StructureInfo> entry : allStructures.getMap().entrySet())
            {
                StructureInfo info = entry.getValue();
                String key = entry.getKey();
                StructureData structureData = this.structureData.getMap().get(key);

                if (!structureData.disabled
                        && RCConfig.shouldStructureGenerate(key, structureData.domain)
                        && info.areDependenciesResolved())
                    generatingStructures.add(key);
            }
        }
    }

    public Set<StructureInfo> getAllGeneratingStructures()
    {
        ensureGenerationCache();
        return Collections.unmodifiableSet(Maps.filterKeys(allStructures.getMap(), input -> generatingStructures.contains(input)).values());
    }

    public Set<String> getAllGeneratingStructureKeys()
    {
        return Collections.unmodifiableSet(generatingStructures);
    }

    public boolean isStructureGenerating(String key)
    {
        ensureGenerationCache();
        return generatingStructures.contains(key);
    }

    public Map<String, StructureInfo> structureMap()
    {
        return Collections.unmodifiableMap(allStructures.getMap());
    }

    @Deprecated
    public Set<String> getAllStructureNames()
    {
        return allStructureIDs();
    }

    public Set<String> allStructureIDs()
    {
        return Collections.unmodifiableSet(allStructures.getMap().keySet());
    }

    protected <T extends StructureGenerationInfo> Collection<Pair<StructureInfo, T>> getCachedGeneration(Class<T> clazz)
    {
        return (Collection<Pair<StructureInfo, T>>) ((Map) cachedGeneration).get(clazz);
    }

    public <T extends StructureGenerationInfo> Collection<Pair<StructureInfo, T>> getStructureGenerations(Class<T> clazz)
    {
        Collection<Pair<StructureInfo, T>> pairs = getCachedGeneration(clazz);
        if (pairs != null)
            return pairs;

        pairs = new ArrayList<>();
        for (StructureInfo info : getAllGeneratingStructures())
        {
            List<T> generationInfos = info.generationInfos(clazz);
            for (T t : generationInfos)
                pairs.add(Pair.of(info, t));
        }

        ((ArrayList) pairs).trimToSize();
        cachedGeneration.put(clazz, (Collection) pairs);

        return pairs;
    }

    public <T extends StructureGenerationInfo> Collection<Pair<StructureInfo, T>> getStructureGenerations(Class<T> clazz, final Predicate<Pair<StructureInfo, T>> predicate)
    {
        return Collections2.filter(getStructureGenerations(clazz), predicate::test);
    }

    public StructureSelector getStructureSelector(BiomeGenBase biome, WorldProvider provider)
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

    public Collection<Pair<StructureInfo, StructureListGenerationInfo>> getStructuresInList(final String listID, final ForgeDirection front)
    {
        return getStructureGenerations(StructureListGenerationInfo.class, input -> listID.equals(input.getRight().listID)
                && (front == null || input.getLeft().isRotatable() || input.getRight().front == front));
    }

    public Collection<Pair<StructureInfo, MazeGenerationInfo>> getStructuresInMaze(final String mazeID)
    {
        return getStructureGenerations(MazeGenerationInfo.class, input -> {
            MazeGenerationInfo info = input.getRight();
            return mazeID.equals(info.mazeID) && info.mazeComponent.isValid();
        });
    }

    private boolean chunkContains(int chunkX, int chunkZ, int x, int z)
    {
        return (x >> 4) == chunkX && (z >> 4) == chunkZ;
    }

    public Collection<Pair<StructureInfo, StaticGenerationInfo>> getStaticStructuresAt(final int chunkX, final int chunkZ, final World world, final ChunkCoordinates spawnPos)
    {
        return getStructureGenerations(StaticGenerationInfo.class, input -> {
            StaticGenerationInfo info = input.getRight();
            return info.dimensionMatcher.apply(world.provider)
                    && chunkContains(chunkX, chunkZ, info.getPositionX(spawnPos), info.getPositionZ(spawnPos)
            );
        });
    }

    public SerializableStringTypeRegistry<Transformer> getTransformerRegistry()
    {
        return transformerRegistry;
    }

    public SerializableStringTypeRegistry<StructureGenerationInfo> getStructureGenerationInfoRegistry()
    {
        return structureGenerationInfoRegistry;
    }

    private void clearCaches()
    {
        structureSelectors.clear();
        cachedGeneration.clear();
        needsGenerationCacheUpdate = true;

        updateVanillaGenerations();
        for (Pair<StructureInfo, VanillaStructureGenerationInfo> pair : getStructureGenerations(VanillaStructureGenerationInfo.class))
        {
            String structureID = structureID(pair.getLeft());
            String generationID = pair.getRight().id();
            Class clazz = GenericVillageCreationHandler.getPieceClass(structureID, generationID);
            if (clazz != null)
                MapGenStructureIO.func_143031_a(clazz, "Rc:" + structureID + "_" + generationID);
        }
    }

    private void updateVanillaGenerations()
    {
        TemporaryVillagerRegistry.instance().setHandlers(
                Sets.newHashSet(Collections2.transform(getStructureGenerations(VanillaStructureGenerationInfo.class),
                        input -> GenericVillageCreationHandler.forGeneration(structureID(input.getLeft()), input.getRight().id())).stream().filter(Objects::nonNull).collect(Collectors.toList()))
        );
    }

    private static class StructureData
    {
        public boolean disabled;
        public String domain;

        public StructureData(boolean disabled, String domain)
        {
            this.disabled = disabled;
            this.domain = domain;
        }
    }
}
