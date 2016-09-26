/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceVanillaDecorationGenerationInfo;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.Placer;
import ivorius.reccomplex.structures.generic.BiomeGenerationInfo;
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import ivorius.reccomplex.structures.generic.placement.GenericPlacer;
import ivorius.reccomplex.structures.generic.presets.BiomeMatcherPresets;
import ivorius.reccomplex.structures.generic.presets.DimensionMatcherPresets;
import ivorius.reccomplex.utils.presets.PresettedList;
import ivorius.reccomplex.utils.presets.PresettedObjects;
import ivorius.reccomplex.worldgen.decoration.RCBiomeDecorator;
import ivorius.reccomplex.worldgen.selector.EnvironmentalSelection;
import ivorius.reccomplex.worldgen.selector.StructureSelector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by lukas on 19.01.15.
 */
public class VanillaDecorationGenerationInfo extends StructureGenerationInfo implements EnvironmentalSelection<RCBiomeDecorator.DecorationType>
{
    private static Gson gson = createGson();

    public Double generationWeight;
    public final PresettedList<BiomeGenerationInfo> biomeWeights = new PresettedList<>(BiomeMatcherPresets.instance(), null);
    public final PresettedList<DimensionGenerationInfo> dimensionWeights = new PresettedList<>(DimensionMatcherPresets.instance(), null);

    public RCBiomeDecorator.DecorationType type;

    public BlockPos spawnShift;

    public VanillaDecorationGenerationInfo()
    {
        this(null, null, RCBiomeDecorator.DecorationType.TREE, BlockPos.ORIGIN);

        biomeWeights.setToDefault();
        dimensionWeights.setToDefault();
    }

    public VanillaDecorationGenerationInfo(@Nullable String id, Double generationWeight, RCBiomeDecorator.DecorationType type, BlockPos spawnShift)
    {
        super(id != null ? id : randomID(VanillaDecorationGenerationInfo.class));
        this.type = type;
        this.generationWeight = generationWeight;
        this.spawnShift = spawnShift;
    }

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(VanillaDecorationGenerationInfo.class, new VanillaDecorationGenerationInfo.Serializer());
        builder.registerTypeAdapter(BiomeGenerationInfo.class, new BiomeGenerationInfo.Serializer());
        builder.registerTypeAdapter(DimensionGenerationInfo.class, new DimensionGenerationInfo.Serializer());

        return builder.create();
    }

    public static Gson getGson()
    {
        return gson;
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

    public void setSpawnShift(BlockPos spawnShift)
    {
        this.spawnShift = spawnShift;
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
        return GenericPlacer.surfacePlacer();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceVanillaDecorationGenerationInfo(navigator, delegate, this);
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

    public static class Serializer implements JsonSerializer<VanillaDecorationGenerationInfo>, JsonDeserializer<VanillaDecorationGenerationInfo>
    {
        @Override
        public VanillaDecorationGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "vanillaStructureSpawnInfo");

            String id = JsonUtils.getString(jsonObject, "id", null);

            Double spawnWeight = jsonObject.has("generationWeight") ? JsonUtils.getDouble(jsonObject, "generationWeight") : null;
            RCBiomeDecorator.DecorationType type = context.deserialize(jsonObject.get("type"), RCBiomeDecorator.DecorationType.class);

            int spawnX = JsonUtils.getInt(jsonObject, "spawnShiftX", 0);
            int spawnY = JsonUtils.getInt(jsonObject, "spawnShiftY", 0);
            int spawnZ = JsonUtils.getInt(jsonObject, "spawnShiftZ", 0);

            VanillaDecorationGenerationInfo genInfo = new VanillaDecorationGenerationInfo(id, spawnWeight, type, new BlockPos(spawnX, spawnY, spawnZ));

            PresettedObjects.read(jsonObject, gson, genInfo.biomeWeights, "biomeWeightsPreset", "generationBiomes", new TypeToken<ArrayList<BiomeGenerationInfo>>() {}.getType());
            PresettedObjects.read(jsonObject, gson, genInfo.dimensionWeights, "dimensionWeightsPreset", "generationDimensions", new TypeToken<ArrayList<DimensionGenerationInfo>>() {}.getType());

            return genInfo;
        }

        @Override
        public JsonElement serialize(VanillaDecorationGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            if (src.generationWeight != null)
                jsonObject.addProperty("generationWeight", src.generationWeight);
            jsonObject.add("type", context.serialize(src.type));

            jsonObject.addProperty("spawnShiftX", src.spawnShift.getX());
            jsonObject.addProperty("spawnShiftY", src.spawnShift.getY());
            jsonObject.addProperty("spawnShiftZ", src.spawnShift.getZ());

            PresettedObjects.write(jsonObject, gson, src.biomeWeights, "biomeWeightsPreset", "generationBiomes");
            PresettedObjects.write(jsonObject, gson, src.dimensionWeights, "dimensionWeightsPreset", "generationDimensions");

            return jsonObject;
        }
    }
}
