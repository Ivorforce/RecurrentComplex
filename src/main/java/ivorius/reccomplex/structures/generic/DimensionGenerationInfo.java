/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

import com.google.gson.*;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.json.JsonUtils;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldProvider;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 24.05.14.
 */
public class DimensionGenerationInfo
{
    private DimensionSelector dimensionSelector;
    private Double generationWeight;

    public DimensionGenerationInfo(String dimensionID, Double generationWeight)
    {
        this.dimensionSelector = new DimensionSelector(dimensionID);
        this.generationWeight = generationWeight;
    }

    public static List<DimensionGenerationInfo> overworldGenerationList()
    {
        return Arrays.asList(new DimensionGenerationInfo("Type:" + DimensionDictionary.UNCATEGORIZED, null),
                new DimensionGenerationInfo(String.format("Type:%s,%s,%s", DimensionDictionary.NO_TOP_LIMIT, DimensionDictionary.BOTTOM_LIMIT, DimensionDictionary.INFINITE), null));
    }

    public static List<DimensionGenerationInfo> netherGenerationList()
    {
        return Arrays.asList(new DimensionGenerationInfo(String.format("Type:%s,%s,%s", DimensionDictionary.HELL, DimensionDictionary.TOP_LIMIT, DimensionDictionary.BOTTOM_LIMIT), null));
    }

    public static List<DimensionGenerationInfo> endGenerationList()
    {
        return Arrays.asList(new DimensionGenerationInfo(String.format("Type:%s,%s,%s", DimensionDictionary.ENDER, DimensionDictionary.NO_TOP_LIMIT, DimensionDictionary.NO_BOTTOM_LIMIT), null));
    }

    public String getDimensionID()
    {
        return dimensionSelector.getDimensionID();
    }

    public void setDimensionID(String dimensionID)
    {
        dimensionSelector.setDimensionID(dimensionID);
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
        return dimensionSelector.matches(provider);
    }

    public DimensionSelector getDimensionSelector()
    {
        return dimensionSelector;
    }

    public String getDisplayString()
    {
        String dimensionID = dimensionSelector.getDimensionID();
        if (dimensionSelector.isTypeList())
            return EnumChatFormatting.AQUA + dimensionID.substring(5) + EnumChatFormatting.RESET;
        else
            return dimensionID;
    }

    public static class Serializer implements JsonDeserializer<DimensionGenerationInfo>, JsonSerializer<DimensionGenerationInfo>
    {
        @Override
        public DimensionGenerationInfo deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "generationInfo");

            String dimensionID = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "dimensionID");
            Double weight = jsonobject.has("weight") ? JsonUtils.getJsonObjectDoubleFieldValue(jsonobject, "weight") : null;

            return new DimensionGenerationInfo(dimensionID, weight);
        }

        @Override
        public JsonElement serialize(DimensionGenerationInfo generationInfo, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("dimensionID", generationInfo.getDimensionID());
            if (generationInfo.generationWeight != null)
                jsonobject.addProperty("weight", generationInfo.generationWeight);

            return jsonobject;
        }
    }
}
