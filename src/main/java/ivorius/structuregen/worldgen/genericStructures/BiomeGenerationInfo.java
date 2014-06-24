/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.worldgen.genericStructures;

import com.google.gson.*;
import ivorius.structuregen.ivtoolkit.tools.IvGsonHelper;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.BiomeDictionary;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 24.05.14.
 */
public class BiomeGenerationInfo
{
    private String biomeID;
    private Integer generationWeight;

    public BiomeGenerationInfo(String biomeID, Integer generationWeight)
    {
        this.biomeID = biomeID;
        this.generationWeight = generationWeight;
    }

    public static List<BiomeGenerationInfo> defaultBiomeGenerationList()
    {
        return Arrays.asList(new BiomeGenerationInfo("Type:PLAINS", null),
                new BiomeGenerationInfo("Type:FOREST", null),
                new BiomeGenerationInfo("Type:MOUNTAIN", null),
                new BiomeGenerationInfo("Type:HILLS", null),
                new BiomeGenerationInfo("Type:SWAMP", null),
                new BiomeGenerationInfo("Type:DESERT", null),
                new BiomeGenerationInfo("Type:FROZEN", null),
                new BiomeGenerationInfo("Type:FOREST,FROZEN", null),
                new BiomeGenerationInfo("Type:JUNGLE", null));
    }

    public static List<BiomeGenerationInfo> oceanBiomeGenerationList()
    {
        return Arrays.asList(new BiomeGenerationInfo("River", 0),
                new BiomeGenerationInfo("Type:WATER", null),
                new BiomeGenerationInfo("Type:WATER,FROZEN", null),
                new BiomeGenerationInfo("Deep Ocean", null));
    }

    public String getBiomeID()
    {
        return biomeID;
    }

    public void setBiomeID(String biomeID)
    {
        this.biomeID = biomeID;
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

    public List<BiomeDictionary.Type> getBiomeTypes()
    {
        if (biomeID.startsWith("Type:"))
        {
            String[] typeIDs = biomeID.substring(5).split(",");

            List<BiomeDictionary.Type> types = new ArrayList<>(typeIDs.length);

            for (String typeID : typeIDs)
            {
                BiomeDictionary.Type type = IvGsonHelper.enumForName(typeID, BiomeDictionary.Type.values());

                if (type == null)
                    return null;

                types.add(type);
            }

            return types;
        }

        return null;
    }

    public static class Serializer implements JsonDeserializer<BiomeGenerationInfo>, JsonSerializer<BiomeGenerationInfo>
    {
        @Override
        public BiomeGenerationInfo deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "generationInfo");

            String biomeID = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "biome");
            Integer weight = jsonobject.has("weight") ? JsonUtils.getJsonObjectIntegerFieldValue(jsonobject, "weight") : null;

            return new BiomeGenerationInfo(biomeID, weight);
        }

        @Override
        public JsonElement serialize(BiomeGenerationInfo generationInfo, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("biome", generationInfo.getBiomeID());
            Integer weight = generationInfo.getGenerationWeight();
            if (weight != null)
            {
                jsonobject.addProperty("weight", weight);
            }

            return jsonobject;
        }
    }
}
