/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic;

import com.google.gson.*;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.BiomeExpression;
import net.minecraft.world.biome.Biome;

import java.lang.reflect.Type;
import java.util.regex.Matcher;

/**
 * Created by lukas on 24.05.14.
 */
public class WeightedBiomeMatcher
{
    private BiomeExpression biomeExpression;
    private Double generationWeight;

    public WeightedBiomeMatcher(String expression, Double generationWeight)
    {
        this.biomeExpression = ExpressionCache.of(new BiomeExpression(), expression);
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
        return biomeExpression.test(biome);
    }

    public BiomeExpression getBiomeExpression()
    {
        return biomeExpression;
    }

    public String getDisplayString()
    {
        return biomeExpression.getDisplayString(null);
    }

    public static class Serializer implements JsonDeserializer<WeightedBiomeMatcher>, JsonSerializer<WeightedBiomeMatcher>
    {
        @Override
        public WeightedBiomeMatcher deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
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

            return new WeightedBiomeMatcher(expression, weight);
        }

        @Override
        public JsonElement serialize(WeightedBiomeMatcher generationInfo, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("biomes", generationInfo.getBiomeExpression().getExpression());
            if (generationInfo.generationWeight != null)
                jsonobject.addProperty("weight", generationInfo.generationWeight);

            return jsonobject;
        }
    }
}
