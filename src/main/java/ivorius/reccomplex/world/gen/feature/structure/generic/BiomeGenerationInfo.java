/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic;

import com.google.gson.*;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.BiomeMatcher;
import net.minecraft.world.biome.Biome;

import java.lang.reflect.Type;
import java.util.regex.Matcher;

/**
 * Created by lukas on 24.05.14.
 */
public class BiomeGenerationInfo
{
    private BiomeMatcher biomeMatcher;
    private Double generationWeight;

    public BiomeGenerationInfo(String expression, Double generationWeight)
    {
        this.biomeMatcher = ExpressionCache.of(new BiomeMatcher(), expression);
        this.generationWeight = generationWeight;
    }

    public Double getGenerationWeight()
    {
        return generationWeight;
    }

    public void setGenerationWeight(Double generationWeight)
    {
        this.generationWeight = generationWeight;
    }

    public double getActiveGenerationWeight()
    {
        return generationWeight != null ? generationWeight : 1.0;
    }

    public boolean hasDefaultWeight()
    {
        return generationWeight == null;
    }

    public boolean matches(Biome biome)
    {
        return biomeMatcher.test(biome);
    }

    public BiomeMatcher getBiomeMatcher()
    {
        return biomeMatcher;
    }

    public String getDisplayString()
    {
        return biomeMatcher.getDisplayString(null);
    }

    public static class Serializer implements JsonDeserializer<BiomeGenerationInfo>, JsonSerializer<BiomeGenerationInfo>
    {
        @Override
        public BiomeGenerationInfo deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.asJsonObject(jsonElement, "generationInfo");

            String expression;
            if (jsonobject.has("biome"))
            {
                // Legacy
                expression = JsonUtils.getString(jsonobject, "biome");
                if (expression.startsWith("Type:"))
                    expression = "$" + expression.substring(5).replaceAll(",", Matcher.quoteReplacement(" & $"));
            }
            else
                expression = JsonUtils.getString(jsonobject, "biomes");

            Double weight = jsonobject.has("weight") ? JsonUtils.getDouble(jsonobject, "weight") : null;

            return new BiomeGenerationInfo(expression, weight);
        }

        @Override
        public JsonElement serialize(BiomeGenerationInfo generationInfo, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("biomes", generationInfo.getBiomeMatcher().getExpression());
            if (generationInfo.generationWeight != null)
                jsonobject.addProperty("weight", generationInfo.generationWeight);

            return jsonobject;
        }
    }
}
