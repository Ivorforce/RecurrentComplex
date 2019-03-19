/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.generation;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.client.rendering.MazeVisualizationContext;
import ivorius.reccomplex.files.SimpleLeveledRegistry;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceVanillaDecorationGeneration;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.presets.PresettedList;
import ivorius.reccomplex.utils.presets.PresettedObjects;
import ivorius.reccomplex.world.gen.feature.decoration.RCBiomeDecorator;
import ivorius.reccomplex.world.gen.feature.selector.CachedStructureSelectors;
import ivorius.reccomplex.world.gen.feature.selector.EnvironmentalSelection;
import ivorius.reccomplex.world.gen.feature.selector.StructureSelector;
import ivorius.reccomplex.world.gen.feature.structure.Placer;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.WeightedBiomeMatcher;
import ivorius.reccomplex.world.gen.feature.structure.generic.WeightedDimensionMatcher;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.SelectivePlacer;
import ivorius.reccomplex.world.gen.feature.structure.generic.presets.BiomeMatcherPresets;
import ivorius.reccomplex.world.gen.feature.structure.generic.presets.DimensionMatcherPresets;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by lukas on 19.01.15.
 */
public class VanillaDecorationGeneration extends GenerationType implements EnvironmentalSelection<RCBiomeDecorator.DecorationType>
{
    private static Gson gson = createGson();

    public Double generationWeight;
    public final PresettedList<WeightedBiomeMatcher> biomeWeights = new PresettedList<>(BiomeMatcherPresets.instance(), null);
    public final PresettedList<WeightedDimensionMatcher> dimensionWeights = new PresettedList<>(DimensionMatcherPresets.instance(), null);

    public RCBiomeDecorator.DecorationType type;

    public SelectivePlacer placer;

    public VanillaDecorationGeneration()
    {
        this(null, null, RCBiomeDecorator.DecorationType.TREE, BlockPos.ORIGIN);

        biomeWeights.setPreset("overworld");
        dimensionWeights.setPreset("overworld");
        placer = new SelectivePlacer();
    }

    public VanillaDecorationGeneration(@Nullable String id, Double generationWeight, RCBiomeDecorator.DecorationType type, BlockPos spawnShift)
    {
        super(id != null ? id : randomID(VanillaDecorationGeneration.class));
        this.type = type;
        this.generationWeight = generationWeight;
    }

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(VanillaDecorationGeneration.class, new VanillaDecorationGeneration.Serializer());
        builder.registerTypeAdapter(WeightedBiomeMatcher.class, new WeightedBiomeMatcher.Serializer());
        builder.registerTypeAdapter(WeightedDimensionMatcher.class, new WeightedDimensionMatcher.Serializer());

        return builder.create();
    }

    public static Gson getGson()
    {
        return gson;
    }

    public static CachedStructureSelectors<StructureSelector<VanillaDecorationGeneration, RCBiomeDecorator.DecorationType>> selectors(StructureRegistry registry)
    {
        return registry.module(Cache.class).selectors;
    }

    @Nonnull
    @Override
    public String id()
    {
        return id;
    }

    @Override
    public void setID(@Nonnull String id)
    {
        this.id = id;
    }

    @Override
    public String displayString()
    {
        return IvTranslations.get("reccomplex.generationInfo.decoration.title");
    }

    @Nullable
    @Override
    public Placer placer()
    {
        return placer;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TableDataSource tableDataSource(MazeVisualizationContext mazeVisualizationContext, TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceVanillaDecorationGeneration(navigator, delegate, this);
    }

    public double getActiveGenerationWeight()
    {
        return generationWeight != null ? generationWeight : 1.0;
    }

    @Override
    public double getGenerationWeight(WorldProvider provider, Biome biome)
    {
        return getActiveGenerationWeight() * StructureSelector.generationWeight(provider, biome, biomeWeights, dimensionWeights);
    }

    @Override
    public RCBiomeDecorator.DecorationType generationCategory()
    {
        return type;
    }

    public static class Serializer implements JsonSerializer<VanillaDecorationGeneration>, JsonDeserializer<VanillaDecorationGeneration>
    {
        @Override
        public VanillaDecorationGeneration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "vanillaStructureSpawnInfo");

            String id = readID(jsonObject);

            Double spawnWeight = jsonObject.has("generationWeight") ? JsonUtils.getDouble(jsonObject, "generationWeight") : null;
            RCBiomeDecorator.DecorationType type = context.deserialize(jsonObject.get("type"), RCBiomeDecorator.DecorationType.class);

            int spawnX = JsonUtils.getInt(jsonObject, "spawnShiftX", 0);
            int spawnY = JsonUtils.getInt(jsonObject, "spawnShiftY", 0);
            int spawnZ = JsonUtils.getInt(jsonObject, "spawnShiftZ", 0);

            VanillaDecorationGeneration genInfo = new VanillaDecorationGeneration(id, spawnWeight, type, new BlockPos(spawnX, spawnY, spawnZ));

            PresettedObjects.read(jsonObject, gson, genInfo.biomeWeights, "biomeWeightsPreset", "generationBiomes", new TypeToken<ArrayList<WeightedBiomeMatcher>>() {}.getType());
            PresettedObjects.read(jsonObject, gson, genInfo.dimensionWeights, "dimensionWeightsPreset", "generationDimensions", new TypeToken<ArrayList<WeightedDimensionMatcher>>() {}.getType());

            genInfo.placer = JsonUtils.has(jsonObject, "placer")
                    ? SelectivePlacer.gson.fromJson(JsonUtils.getJsonObject(jsonObject, "placer"), SelectivePlacer.class)
                    : SelectivePlacer.surfacePlacer(0);

            return genInfo;
        }

        @Override
        public JsonElement serialize(VanillaDecorationGeneration src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            if (src.generationWeight != null)
                jsonObject.addProperty("generationWeight", src.generationWeight);
            jsonObject.add("type", context.serialize(src.type));

            PresettedObjects.write(jsonObject, gson, src.biomeWeights, "biomeWeightsPreset", "generationBiomes");
            PresettedObjects.write(jsonObject, gson, src.dimensionWeights, "dimensionWeightsPreset", "generationDimensions");

            jsonObject.add("placer", SelectivePlacer.gson.toJsonTree(src.placer));

            return jsonObject;
        }
    }

    public static class Cache extends SimpleLeveledRegistry.Module<StructureRegistry>
    {
        protected CachedStructureSelectors<StructureSelector<VanillaDecorationGeneration, RCBiomeDecorator.DecorationType>> selectors;

        @Override
        public void setRegistry(StructureRegistry registry)
        {
            selectors = new CachedStructureSelectors<>((biome, worldProvider) ->
                    new StructureSelector<>(registry.activeMap(), worldProvider, biome, VanillaDecorationGeneration.class));
        }

        @Override
        public void invalidate()
        {
            selectors.clear();
        }
    }
}
