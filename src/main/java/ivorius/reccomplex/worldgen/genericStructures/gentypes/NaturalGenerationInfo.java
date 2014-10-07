/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.genericStructures.gentypes;

import com.google.gson.*;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.worldgen.genericStructures.BiomeGenerationInfo;
import ivorius.reccomplex.worldgen.genericStructures.GenerationYSelector;

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
    public final List<BiomeGenerationInfo> generationWeights = new ArrayList<>();

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
        Collections.addAll(naturalGenerationInfo.generationWeights, infos);

        return naturalGenerationInfo;
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

            if (jsonObject.has("generationBiomes"))
            {
                BiomeGenerationInfo[] infos = context.deserialize(jsonObject.get("generationBiomes"), BiomeGenerationInfo[].class);
                Collections.addAll(naturalGenerationInfo.generationWeights, infos);
            }
            else
            {
                RecurrentComplex.logger.warn("Structure JSON missing 'generationBiomes'!");
            }

            return naturalGenerationInfo;
        }

        @Override
        public JsonElement serialize(NaturalGenerationInfo src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("generationCategory", src.generationCategory);
            jsonObject.add("generationY", context.serialize(src.ySelector));
            jsonObject.add("generationBiomes", context.serialize(src.generationWeights));

            return jsonObject;
        }
    }
}
