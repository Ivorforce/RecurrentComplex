/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.gson.*;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceNaturalGenerationInfo;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.generic.BiomeGenerationInfo;
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import ivorius.reccomplex.structures.generic.GenerationYSelector;
import ivorius.reccomplex.structures.generic.presets.BiomeMatcherPresets;
import ivorius.reccomplex.structures.generic.presets.DimensionMatcherPresets;
import ivorius.reccomplex.utils.PresettedList;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by lukas on 07.10.14.
 */
public class NaturalGenerationInfo extends StructureGenerationInfo
{
    private static Gson gson = createGson();

    public String id = "";

    public final PresettedList<BiomeGenerationInfo> biomeWeights = new PresettedList<>(BiomeMatcherPresets.instance(), null);
    public final PresettedList<DimensionGenerationInfo> dimensionWeights = new PresettedList<>(DimensionMatcherPresets.instance(), null);

    public String generationCategory;
    public GenerationYSelector ySelector;
    private Double generationWeight;

    public NaturalGenerationInfo()
    {
        this("NaturalGen1", "decoration", new GenerationYSelector(GenerationYSelector.SelectionMode.SURFACE, 0, 0));

        biomeWeights.setToDefault();
        dimensionWeights.setToDefault();
    }

    public NaturalGenerationInfo(String id, String generationCategory, GenerationYSelector ySelector)
    {
        this.id = id;
        this.generationCategory = generationCategory;
        this.ySelector = ySelector;
    }

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(NaturalGenerationInfo.class, new NaturalGenerationInfo.Serializer());
        builder.registerTypeAdapter(BiomeGenerationInfo.class, new BiomeGenerationInfo.Serializer());
        builder.registerTypeAdapter(DimensionGenerationInfo.class, new DimensionGenerationInfo.Serializer());

        return builder.create();
    }

    public static Gson getGson()
    {
        return gson;
    }

    public static NaturalGenerationInfo deserializeFromVersion1(JsonObject jsonObject, JsonDeserializationContext context)
    {
        String generationCategory = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "generationCategory");
        GenerationYSelector ySelector = gson.fromJson(jsonObject.get("generationY"), GenerationYSelector.class);

        NaturalGenerationInfo naturalGenerationInfo = new NaturalGenerationInfo("", generationCategory, ySelector);
        if (jsonObject.has("generationBiomes"))
        {
            BiomeGenerationInfo[] infos = gson.fromJson(jsonObject.get("generationBiomes"), BiomeGenerationInfo[].class);
            naturalGenerationInfo.biomeWeights.setContents(Arrays.asList(infos));
        }
        else
            naturalGenerationInfo.biomeWeights.setToDefault();

        naturalGenerationInfo.dimensionWeights.setToDefault();

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

    public double getGenerationWeight(BiomeGenBase biome, WorldProvider provider)
    {
        return getActiveSpawnWeight()
                * generationWeightInBiome(biome)
                * generationWeightInDimension(provider);
    }

    public double generationWeightInDimension(WorldProvider provider)
    {
        for (DimensionGenerationInfo generationInfo : dimensionWeights.list)
        {
            if (generationInfo.matches(provider))
                return generationInfo.getActiveGenerationWeight();
        }

        return 0;
    }

    public double generationWeightInBiome(BiomeGenBase biome)
    {
        for (BiomeGenerationInfo generationInfo : biomeWeights.list)
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
        return StatCollector.translateToLocal("reccomplex.generationInfo.natural");
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceNaturalGenerationInfo(navigator, delegate, this);
    }

    public static class Serializer implements JsonSerializer<NaturalGenerationInfo>, JsonDeserializer<NaturalGenerationInfo>
    {
        @Override
        public NaturalGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "naturalGenerationInfo");

            String id = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "id", "");

            String generationCategory = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "generationCategory");
            GenerationYSelector ySelector;

            if (jsonObject.has("generationY"))
                ySelector = gson.fromJson(jsonObject.get("generationY"), GenerationYSelector.class);
            else
            {
                RecurrentComplex.logger.warn("Structure JSON missing 'generationY'! Using 'surface'!");
                ySelector = new GenerationYSelector(GenerationYSelector.SelectionMode.SURFACE, 0, 0);
            }

            NaturalGenerationInfo naturalGenerationInfo = new NaturalGenerationInfo(id, generationCategory, ySelector);

            if (jsonObject.has("generationWeight"))
                naturalGenerationInfo.generationWeight = JsonUtils.getJsonObjectDoubleFieldValue(jsonObject, "generationWeight");

            if (!naturalGenerationInfo.biomeWeights.setPreset(JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "biomeWeightsPreset", null)))
            {
                if (jsonObject.has("generationBiomes"))
                    Collections.addAll(naturalGenerationInfo.biomeWeights.list, gson.fromJson(jsonObject.get("generationBiomes"), BiomeGenerationInfo[].class));
                else
                    naturalGenerationInfo.biomeWeights.setToDefault();
            }

            if (!naturalGenerationInfo.dimensionWeights.setPreset(JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "dimensionWeightsPreset", null)))
            {
                if (jsonObject.has("generationDimensions"))
                    Collections.addAll(naturalGenerationInfo.dimensionWeights.list, gson.fromJson(jsonObject.get("generationDimensions"), DimensionGenerationInfo[].class));
                else
                    naturalGenerationInfo.dimensionWeights.setToDefault();
            }

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

            jsonObject.add("generationY", gson.toJsonTree(src.ySelector));

            if (src.biomeWeights.getPreset() != null)
                jsonObject.addProperty("biomeWeightsPreset", src.biomeWeights.getPreset());
            jsonObject.add("generationBiomes", gson.toJsonTree(src.biomeWeights.list));

            if (src.dimensionWeights.getPreset() != null)
                jsonObject.addProperty("dimensionWeightsPreset", src.dimensionWeights.getPreset());
            jsonObject.add("generationDimensions", gson.toJsonTree(src.dimensionWeights.list));

            return jsonObject;
        }
    }
}
