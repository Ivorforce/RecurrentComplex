/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.gson.*;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.generic.BiomeGenerationInfo;
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import ivorius.reccomplex.structures.generic.GenerationYSelector;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 07.10.14.
 */
public class NaturalGenerationInfo
{
    public String generationCategory;
    public GenerationYSelector ySelector;

    private Double generationWeight;
    public final List<BiomeGenerationInfo> biomeWeights = new ArrayList<>();
    public final List<DimensionGenerationInfo> dimensionWeights = new ArrayList<>();

    public NaturalGenerationInfo(String generationCategory, GenerationYSelector ySelector)
    {
        this.generationCategory = generationCategory;
        this.ySelector = ySelector;
    }

    public static NaturalGenerationInfo deserializeFromVersion1(JsonObject jsonObject, JsonDeserializationContext context)
    {
        String generationCategory = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "generationCategory");
        GenerationYSelector ySelector = context.deserialize(jsonObject.get("generationY"), GenerationYSelector.class);

        NaturalGenerationInfo naturalGenerationInfo = new NaturalGenerationInfo(generationCategory, ySelector);
        BiomeGenerationInfo[] infos = context.deserialize(jsonObject.get("generationBiomes"), BiomeGenerationInfo[].class);
        Collections.addAll(naturalGenerationInfo.biomeWeights, infos);

        naturalGenerationInfo.dimensionWeights.addAll(DimensionGenerationInfo.overworldGenerationList());

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
        for (DimensionGenerationInfo generationInfo : dimensionWeights)
        {
            if (generationInfo.matches(provider))
                return generationInfo.getActiveGenerationWeight();
        }

        return 0;
    }

    public double generationWeightInBiome(BiomeGenBase biome)
    {
        for (BiomeGenerationInfo generationInfo : biomeWeights)
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

    public static class Serializer implements JsonSerializer<NaturalGenerationInfo>, JsonDeserializer<NaturalGenerationInfo>
    {
        @Override
        public NaturalGenerationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "naturalGenerationInfo");

            String generationCategory = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "generationCategory");
            GenerationYSelector ySelector;

            if (jsonObject.has("generationY"))
                ySelector = context.deserialize(jsonObject.get("generationY"), GenerationYSelector.class);
            else
            {
                RecurrentComplex.logger.warn("Structure JSON missing 'generationY'! Using 'surface'!");
                ySelector = new GenerationYSelector(GenerationYSelector.SelectionMode.SURFACE, 0, 0);
            }

            NaturalGenerationInfo naturalGenerationInfo = new NaturalGenerationInfo(generationCategory, ySelector);

            if (jsonObject.has("generationWeight"))
                naturalGenerationInfo.generationWeight = JsonUtils.getJsonObjectDoubleFieldValue(jsonObject, "generationWeight");

            if (jsonObject.has("generationBiomes"))
            {
                BiomeGenerationInfo[] infos = context.deserialize(jsonObject.get("generationBiomes"), BiomeGenerationInfo[].class);
                Collections.addAll(naturalGenerationInfo.biomeWeights, infos);
            }
            else
            {
                RecurrentComplex.logger.warn("Structure JSON missing 'generationBiomes'!");
            }

            if (jsonObject.has("generationDimensions"))
            {
                DimensionGenerationInfo[] infos = context.deserialize(jsonObject.get("generationDimensions"), DimensionGenerationInfo[].class);
                Collections.addAll(naturalGenerationInfo.dimensionWeights, infos);
            }
            else
            {
                // Legacy
                naturalGenerationInfo.dimensionWeights.addAll(DimensionGenerationInfo.overworldGenerationList());
            }

            return naturalGenerationInfo;
        }

        @Override
        public JsonElement serialize(NaturalGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("generationCategory", src.generationCategory);
            if (src.generationWeight != null)
                jsonObject.addProperty("generationWeight", src.generationWeight);
            jsonObject.add("generationY", context.serialize(src.ySelector));
            jsonObject.add("generationBiomes", context.serialize(src.biomeWeights));
            jsonObject.add("generationDimensions", context.serialize(src.dimensionWeights));

            return jsonObject;
        }
    }
}
