/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.genericStructures;

import com.google.gson.*;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.json.JsonUtils;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldProvider;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by lukas on 24.05.14.
 */
public class DimensionGenerationInfo
{
    private DimensionSelector dimensionSelector;
    private Integer generationWeight;

    public DimensionGenerationInfo(String dimensionID, Integer generationWeight)
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

    public Integer getGenerationWeight()
    {
        return generationWeight;
    }

    public void setGenerationWeight(Integer generationWeight)
    {
        this.generationWeight = generationWeight;
    }

    public int getActiveGenerationWeight()
    {
        return generationWeight != null ? generationWeight : 100;
    }

    public boolean hasDefaultWeight()
    {
        return generationWeight == null;
    }

    public boolean matches(WorldProvider provider)
    {
        return dimensionSelector.matches(provider);
    }

    public boolean isTypeList()
    {
        return dimensionSelector.isTypeList();
    }

    public Collection<String> allTypes()
    {
        return dimensionSelector.getDimensionTypes();
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
            Integer weight = jsonobject.has("weight") ? JsonUtils.getJsonObjectIntegerFieldValue(jsonobject, "weight") : null;

            return new DimensionGenerationInfo(dimensionID, weight);
        }

        @Override
        public JsonElement serialize(DimensionGenerationInfo generationInfo, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("dimensionID", generationInfo.getDimensionID());
            Integer weight = generationInfo.getGenerationWeight();
            if (weight != null)
            {
                jsonobject.addProperty("weight", weight);
            }

            return jsonobject;
        }
    }
}
