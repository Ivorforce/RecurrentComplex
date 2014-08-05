/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.genericStructures;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

import java.lang.reflect.Type;
import java.util.*;

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

    public static List<BiomeGenerationInfo> overworldBiomeGenerationList()
    {
        return Arrays.asList(new BiomeGenerationInfo("Type:NETHER", 0),
                new BiomeGenerationInfo("Type:END", 0),
                new BiomeGenerationInfo("Type:WATER", 0),
                new BiomeGenerationInfo("Type:PLAINS", null),
                new BiomeGenerationInfo("Type:FOREST", null),
                new BiomeGenerationInfo("Type:MOUNTAIN", null),
                new BiomeGenerationInfo("Type:HILLS", null),
                new BiomeGenerationInfo("Type:SWAMP", null),
                new BiomeGenerationInfo("Type:SANDY", null),
                new BiomeGenerationInfo("Type:MESA", null),
                new BiomeGenerationInfo("Type:WASTELAND", null),
                new BiomeGenerationInfo("Type:MUSHROOM", null),
                new BiomeGenerationInfo("Type:JUNGLE", null));
    }

    public static List<BiomeGenerationInfo> undergroundBiomeGenerationList()
    {
        return Arrays.asList(new BiomeGenerationInfo("Type:NETHER", 0),
                new BiomeGenerationInfo("Type:END", 0),
                new BiomeGenerationInfo("Type:PLAINS", null),
                new BiomeGenerationInfo("Type:FOREST", null),
                new BiomeGenerationInfo("Type:MOUNTAIN", null),
                new BiomeGenerationInfo("Type:HILLS", null),
                new BiomeGenerationInfo("Type:SWAMP", null),
                new BiomeGenerationInfo("Type:SANDY", null),
                new BiomeGenerationInfo("Type:MESA", null),
                new BiomeGenerationInfo("Type:RIVER", null),
                new BiomeGenerationInfo("Type:OCEAN", null),
                new BiomeGenerationInfo("Type:WASTELAND", null),
                new BiomeGenerationInfo("Type:MUSHROOM", null),
                new BiomeGenerationInfo("Type:JUNGLE", null));
    }

    public static List<BiomeGenerationInfo> oceanBiomeGenerationList()
    {
        return Arrays.asList(new BiomeGenerationInfo("Type:NETHER", 0),
                new BiomeGenerationInfo("Type:END", 0),
                new BiomeGenerationInfo("Type:OCEAN,SNOWY", 0),
                new BiomeGenerationInfo("Type:OCEAN", null));
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
                BiomeDictionary.Type type = IvGsonHelper.enumForNameIgnoreCase(typeID, BiomeDictionary.Type.values());

                if (type == null)
                    return null;

                types.add(type);
            }

            return types;
        }

        return null;
    }

    public static Set<BiomeGenBase> gatherAllBiomes()
    {
        Set<BiomeGenBase> set = new HashSet<>();

        for (BiomeGenBase biomeGenBase : BiomeGenBase.getBiomeGenArray())
        {
            if (biomeGenBase != null)
                set.add(biomeGenBase);
        }

        for (BiomeDictionary.Type type : BiomeDictionary.Type.values())
            Collections.addAll(set, BiomeDictionary.getBiomesForType(type));

        return set;
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
