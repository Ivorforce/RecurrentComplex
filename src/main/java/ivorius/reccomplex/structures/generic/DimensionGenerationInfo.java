/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

import com.google.gson.*;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.generic.matchers.DimensionMatcher;
import net.minecraft.world.WorldProvider;

import java.lang.reflect.Type;
import java.util.regex.Matcher;

/**
 * Created by lukas on 24.05.14.
 */
public class DimensionGenerationInfo
{
    private DimensionMatcher dimensionMatcher;
    private Double generationWeight;

    public DimensionGenerationInfo(String expression, Double generationWeight)
    {
        this.dimensionMatcher = new DimensionMatcher(expression);
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

    public boolean matches(WorldProvider provider)
    {
        return dimensionMatcher.test(provider);
    }

    public DimensionMatcher getDimensionMatcher()
    {
        return dimensionMatcher;
    }

    public String getDisplayString()
    {
        return dimensionMatcher.getDisplayString(null);
    }

    public static class Serializer implements JsonDeserializer<DimensionGenerationInfo>, JsonSerializer<DimensionGenerationInfo>
    {
        @Override
        public DimensionGenerationInfo deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.asJsonObject(jsonElement, "generationInfo");

            String expression;
            if (jsonobject.has("dimensionID"))
            {
                // Legacy
                expression = JsonUtils.getString(jsonobject, "dimensionID");
                if (expression.startsWith("Type:"))
                    expression = "$" + expression.substring(5).replaceAll(",", Matcher.quoteReplacement(" & $"));
            }
            else
                expression = JsonUtils.getString(jsonobject, "dimensions");

            Double weight = jsonobject.has("weight") ? JsonUtils.getDouble(jsonobject, "weight") : null;

            return new DimensionGenerationInfo(expression, weight);
        }

        @Override
        public JsonElement serialize(DimensionGenerationInfo generationInfo, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("dimensions", generationInfo.getDimensionMatcher().getExpression());
            if (generationInfo.generationWeight != null)
                jsonobject.addProperty("weight", generationInfo.generationWeight);

            return jsonobject;
        }
    }
}
