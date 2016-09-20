/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceNaturalGenerationInfo;
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
import ivorius.reccomplex.structures.generic.presets.GenericPlacerPresets;
import ivorius.reccomplex.utils.presets.PresettedList;
import ivorius.reccomplex.utils.presets.PresettedObjects;
import ivorius.reccomplex.utils.presets.PresettedObject;
import ivorius.reccomplex.worldgen.StructureGenerationData;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by lukas on 07.10.14.
 */
public class NaturalGenerationInfo extends StructureGenerationInfo
{
    private static Gson gson = createGson();

    public final PresettedList<BiomeGenerationInfo> biomeWeights = new PresettedList<>(BiomeMatcherPresets.instance(), null);
    public final PresettedList<DimensionGenerationInfo> dimensionWeights = new PresettedList<>(DimensionMatcherPresets.instance(), null);

    private Double generationWeight;

    public String generationCategory;

    public PresettedObject<GenericPlacer> placer = new PresettedObject<GenericPlacer>(GenericPlacerPresets.instance(), null);

    public SpawnLimitation spawnLimitation;

    public NaturalGenerationInfo()
    {
        this(null, "decoration");

        biomeWeights.setToDefault();
        dimensionWeights.setToDefault();
        placer.setToDefault();
    }

    public NaturalGenerationInfo(@Nullable String id, String generationCategory)
    {
        super(id != null ? id : randomID(NaturalGenerationInfo.class));
        this.generationCategory = generationCategory;
    }

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(NaturalGenerationInfo.class, new NaturalGenerationInfo.Serializer());
        builder.registerTypeAdapter(BiomeGenerationInfo.class, new BiomeGenerationInfo.Serializer());
        builder.registerTypeAdapter(DimensionGenerationInfo.class, new DimensionGenerationInfo.Serializer());
        builder.registerTypeAdapter(GenericPlacer.class, new GenericPlacer.Serializer());

        return builder.create();
    }

    public static Gson getGson()
    {
        return gson;
    }

    public static NaturalGenerationInfo deserializeFromVersion1(JsonObject jsonObject, JsonDeserializationContext context)
    {
        String generationCategory = JsonUtils.getString(jsonObject, "generationCategory");

        NaturalGenerationInfo naturalGenerationInfo = new NaturalGenerationInfo("", generationCategory);
        if (jsonObject.has("generationBiomes"))
        {
            BiomeGenerationInfo[] infos = gson.fromJson(jsonObject.get("generationBiomes"), BiomeGenerationInfo[].class);
            naturalGenerationInfo.biomeWeights.setContents(Arrays.asList(infos));
        }
        else
            naturalGenerationInfo.biomeWeights.setToDefault();

        naturalGenerationInfo.dimensionWeights.setToDefault();

        GenericPlacer.Serializer.readLegacyPlacer(naturalGenerationInfo.placer, context, JsonUtils.getJsonObject(jsonObject, "generationY", new JsonObject()));

        return naturalGenerationInfo;
    }

    public Double getGenerationWeight()
    {
        return generationWeight;
    }

    public void setGenerationWeight(Double generationWeight)
    {
        this.generationWeight = generationWeight;
    }

    public double getGenerationWeight(Biome biome, WorldProvider provider)
    {
        return getActiveSpawnWeight()
                * generationWeightInBiome(biome)
                * generationWeightInDimension(provider);
    }

    public double generationWeightInDimension(WorldProvider provider)
    {
        for (DimensionGenerationInfo generationInfo : dimensionWeights.getContents())
        {
            if (generationInfo.matches(provider))
                return generationInfo.getActiveGenerationWeight();
        }

        return 0;
    }

    public double generationWeightInBiome(Biome biome)
    {
        for (BiomeGenerationInfo generationInfo : biomeWeights.getContents())
        {
            if (generationInfo.matches(biome))
                return generationInfo.getActiveGenerationWeight();
        }

        return 0;
    }

    public double getActiveSpawnWeight()
    {
        return generationWeight != null ? generationWeight : 1.0;
    }

    public boolean hasDefaultWeight()
    {
        return generationWeight == null;
    }

    public boolean hasLimitations()
    {
        return spawnLimitation != null;
    }

    public SpawnLimitation getLimitations()
    {
        return spawnLimitation;
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
        return IvTranslations.get("reccomplex.generationInfo.natural");
    }

    @Nullable
    @Override
    public Placer placer()
    {
        return placer.getContents();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceNaturalGenerationInfo(navigator, delegate, this);
    }

    public static class SpawnLimitation
    {
        public int maxCount = 1;
        public Context context = Context.DIMENSION;

        public boolean areResolved(World world, String structureID)
        {
            return StructureGenerationData.get(world).getEntriesByID(structureID).size() < maxCount;
        }

        public enum Context
        {
            @SerializedName("dimension")
            DIMENSION,
        }
    }

    public static class Serializer implements JsonSerializer<NaturalGenerationInfo>, JsonDeserializer<NaturalGenerationInfo>
    {
        @Override
        public NaturalGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "naturalGenerationInfo");

            String id = JsonUtils.getString(jsonObject, "id", null);

            String generationCategory = JsonUtils.getString(jsonObject, "generationCategory");

            NaturalGenerationInfo naturalGenerationInfo = new NaturalGenerationInfo(id, generationCategory);

            if (!PresettedObjects.read(jsonObject, gson, naturalGenerationInfo.placer, "placerPreset", "placer", new TypeToken<GenericPlacer>(){}.getType())
                    && jsonObject.has("generationY"))
            {
                // Legacy
                GenericPlacer.Serializer.readLegacyPlacer(naturalGenerationInfo.placer, context, JsonUtils.getJsonObject(jsonObject, "generationY", new JsonObject()));
            }

            if (jsonObject.has("generationWeight"))
                naturalGenerationInfo.generationWeight = JsonUtils.getDouble(jsonObject, "generationWeight");

            PresettedObjects.read(jsonObject, gson, naturalGenerationInfo.biomeWeights, "biomeWeightsPreset", "generationBiomes", new TypeToken<ArrayList<BiomeGenerationInfo>>(){}.getType());
            PresettedObjects.read(jsonObject, gson, naturalGenerationInfo.dimensionWeights, "dimensionWeightsPreset", "generationDimensions", new TypeToken<ArrayList<DimensionGenerationInfo>>(){}.getType());

            if (jsonObject.has("spawnLimitation"))
                naturalGenerationInfo.spawnLimitation = context.deserialize(jsonObject.get("spawnLimitation"), SpawnLimitation.class);

            return naturalGenerationInfo;
        }

        @Override
        public JsonElement serialize(NaturalGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            jsonObject.addProperty("generationCategory", src.generationCategory);
            if (src.generationWeight != null)
                jsonObject.addProperty("generationWeight", src.generationWeight);

            PresettedObjects.write(jsonObject, gson, src.placer, "placerPreset", "placer");

            PresettedObjects.write(jsonObject, gson, src.biomeWeights, "biomeWeightsPreset", "generationBiomes");
            PresettedObjects.write(jsonObject, gson, src.dimensionWeights, "dimensionWeightsPreset", "generationDimensions");

            if (src.spawnLimitation != null)
                jsonObject.add("spawnLimitation", context.serialize(src.spawnLimitation));

            return jsonObject;
        }

    }
}
