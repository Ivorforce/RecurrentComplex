/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.ivtoolkit.random.WeightedSelector;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

import java.lang.reflect.Type;

/**
 * Created by lukas on 03.03.15.
 */
public class WeightedBlockState implements WeightedSelector.Item
{
    private static Gson gson = createGson();

    public Double weight;

    public Block block;
    public int metadata;

    public String tileEntityInfo;

    public WeightedBlockState(Double weight, Block block, int metadata, String tileEntityInfo)
    {
        this.weight = weight;
        this.block = block;
        this.metadata = metadata;
        this.tileEntityInfo = tileEntityInfo;
    }

    public WeightedBlockState(MCRegistry registry, NBTTagCompound compound)
    {
        weight = compound.hasKey("weight") ? compound.getDouble("weight") : null;
        block = compound.hasKey("block") ? registry.blockFromID(compound.getString("block")) : null;
        metadata = compound.getInteger("meta");
        tileEntityInfo = compound.getString("tileEntityInfo");
    }

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(WeightedBlockState.class, new WeightedBlockState.Serializer(RecurrentComplex.specialRegistry));

        return builder.create();
    }

    public static Gson getGson()
    {
        return gson;
    }

    @Override
    public double getWeight()
    {
        return weight != null ? weight : 1.0;
    }

    public NBTTagCompound writeToNBT(MCRegistry registry)
    {
        NBTTagCompound compound = new NBTTagCompound();

        if (weight != null) compound.setDouble("weight", weight);
        if (block != null) compound.setString("block", registry.idFromBlock(block));
        compound.setInteger("meta", metadata);
        compound.setString("tileEntityInfo", tileEntityInfo);

        return compound;
    }

    public static class Serializer implements JsonDeserializer<WeightedBlockState>, JsonSerializer<WeightedBlockState>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public WeightedBlockState deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "blockState");

            Double weight = jsonObject.has("weight") ? JsonUtils.getJsonObjectDoubleFieldValue(jsonObject, "weight") : null;

            String block = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "block", "air");
            int metadata = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "metadata", 0);

            String tileEntityInfo = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "tileEntityInfo", "");

            return new WeightedBlockState(weight, registry.blockFromID(block), metadata, tileEntityInfo);
        }

        @Override
        public JsonElement serialize(WeightedBlockState generationInfo, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            if (generationInfo.weight != null)
                jsonObject.addProperty("weight", generationInfo.weight);

            jsonObject.addProperty("block", registry.idFromBlock(generationInfo.block));
            jsonObject.addProperty("metadata", generationInfo.metadata);

            jsonObject.addProperty("tileEntityInfo", generationInfo.tileEntityInfo);

            return jsonObject;
        }
    }
}
