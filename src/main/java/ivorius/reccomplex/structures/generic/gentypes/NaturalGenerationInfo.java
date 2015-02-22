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
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 07.10.14.
 */
public class NaturalGenerationInfo extends StructureGenerationInfo
{
    private static Gson gson = createGson();

    public final List<BiomeGenerationInfo> biomeWeights = new ArrayList<>();
    public final List<DimensionGenerationInfo> dimensionWeights = new ArrayList<>();

    public String generationCategory;
    public GenerationYSelector ySelector;
    private Double generationWeight;

    public NaturalGenerationInfo()
    {
        this("decoration", new GenerationYSelector(GenerationYSelector.SelectionMode.SURFACE, 0, 0));
        biomeWeights.addAll(BiomeGenerationInfo.overworldBiomeGenerationList());
        dimensionWeights.addAll(DimensionGenerationInfo.overworldGenerationList());
    }

    public NaturalGenerationInfo(String generationCategory, GenerationYSelector ySelector)
    {
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

        NaturalGenerationInfo naturalGenerationInfo = new NaturalGenerationInfo(generationCategory, ySelector);
        BiomeGenerationInfo[] infos = gson.fromJson(jsonObject.get("generationBiomes"), BiomeGenerationInfo[].class);
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

            String generationCategory = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "generationCategory");
            GenerationYSelector ySelector;

            if (jsonObject.has("generationY"))
                ySelector = gson.fromJson(jsonObject.get("generationY"), GenerationYSelector.class);
            else
            {
                RecurrentComplex.logger.warn("Structure JSON missing 'generationY'! Using 'surface'!");
                ySelector = new GenerationYSelector(GenerationYSelector.SelectionMode.SURFACE, 0, 0);
            }

            NaturalGenerationInfo naturalGenerationInfo = new NaturalGenerationInfo(generationCategory, ySelector);

            if (jsonObject.has("generationWeight"))
                naturalGenerationInfo.generationWeight = JsonUtils.getJsonObjectDoubleFieldValue(jsonObject, "generationWeight");

            if (jsonObject.has("generationBiomes"))
                Collections.addAll(naturalGenerationInfo.biomeWeights, gson.fromJson(jsonObject.get("generationBiomes"), BiomeGenerationInfo[].class));

            if (jsonObject.has("generationDimensions"))
                Collections.addAll(naturalGenerationInfo.dimensionWeights, gson.fromJson(jsonObject.get("generationDimensions"), DimensionGenerationInfo[].class));
            else
                naturalGenerationInfo.dimensionWeights.addAll(DimensionGenerationInfo.overworldGenerationList()); // Legacy

            return naturalGenerationInfo;
        }

        @Override
        public JsonElement serialize(NaturalGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("generationCategory", src.generationCategory);
            if (src.generationWeight != null)
                jsonObject.addProperty("generationWeight", src.generationWeight);

            jsonObject.add("generationY", gson.toJsonTree(src.ySelector));

            if (src.biomeWeights.size() > 0)
                jsonObject.add("generationBiomes", gson.toJsonTree(src.biomeWeights));

            // Always add to prevent legacy problems
            jsonObject.add("generationDimensions", gson.toJsonTree(src.dimensionWeights));

            return jsonObject;
        }
    }
}
