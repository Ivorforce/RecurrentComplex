/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockStates;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.json.NBTToJson;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Created by lukas on 03.03.15.
 */
public class WeightedBlockState implements WeightedSelector.Item
{
    private static Gson gson = createGson();

    @Nullable
    public Double weight;

    @Nullable
    public IBlockState state;

    @Nullable
    public NBTTagCompound tileEntityInfo;

    public WeightedBlockState(@Nullable Double weight, @Nullable IBlockState state, @Nullable NBTTagCompound tileEntityInfo)
    {
        this.weight = weight;
        this.state = state;
        this.tileEntityInfo = tileEntityInfo;
    }

    public WeightedBlockState(MCRegistry registry, NBTTagCompound compound)
    {
        weight = compound.hasKey("weight") ? compound.getDouble("weight") : null;
        Block block = compound.hasKey("block") ? registry.blockFromID(new ResourceLocation(compound.getString("block"))) : null;
        state = block != null ? BlockStates.fromMetadata(block, compound.getInteger("meta")) : null;
        tileEntityInfo = compound.hasKey("tileEntityInfo") ? tryParse(compound.getString("tileEntityInfo")) // Legacy
                : compound.hasKey("tileEntity") ? (NBTTagCompound) compound.getTag("tileEntity") : null;
    }

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(WeightedBlockState.class, new WeightedBlockState.Serializer(RecurrentComplex.specialRegistry));
        builder.registerTypeAdapter(WeightedBlockState.class, new WeightedBlockState.Serializer(RecurrentComplex.specialRegistry));
        NBTToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public static Gson getGson()
    {
        return gson;
    }

    public static NBTTagCompound tryParse(String json)
    {
        if (json.trim().length() == 0)
            return null;

        NBTTagCompound nbt = null;

        try
        {
            nbt = JsonToNBT.getTagFromJson(json);
        }
        catch (NBTException ignored)
        {

        }

        return nbt;
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
        if (state != null)
        {
            compound.setString("block", registry.idFromBlock(state.getBlock()).toString());
            compound.setInteger("meta", BlockStates.toMetadata(state));
        }
        if (tileEntityInfo != null) compound.setTag("tileEntity", tileEntityInfo);

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
            JsonObject jsonObject = JsonUtils.asJsonObject(jsonElement, "blockState");

            Double weight = jsonObject.has("weight") ? JsonUtils.getDouble(jsonObject, "weight") : null;

            IBlockState state = BlockStates.fromMetadata(registry.blockFromID(new ResourceLocation(JsonUtils.getString(jsonObject, "block", "air")))
                    , JsonUtils.getInt(jsonObject, "metadata", 0));

            NBTTagCompound tileEntityInfo = JsonUtils.hasString(jsonObject, "tileEntityInfo")
                    ? tryParse(JsonUtils.getString(jsonObject, "tileEntityInfo")) // Legacy
                    : jsonObject.has("tileEntity") ? getGson().fromJson(jsonObject.get("tileEntity"), NBTTagCompound.class) : null;

            return new WeightedBlockState(weight, state, tileEntityInfo);
        }

        @Override
        public JsonElement serialize(WeightedBlockState source, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            if (source.weight != null)
                jsonObject.addProperty("weight", source.weight);

            jsonObject.addProperty("block", registry.idFromBlock(source.state.getBlock()).toString());
            jsonObject.addProperty("metadata", ivorius.ivtoolkit.blocks.BlockStates.toMetadata(source.state));

            jsonObject.addProperty("tileEntity", gson.toJson(source.tileEntityInfo));

            return jsonObject;
        }
    }
}
