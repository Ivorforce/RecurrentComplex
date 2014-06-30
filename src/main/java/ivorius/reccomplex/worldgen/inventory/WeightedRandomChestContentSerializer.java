/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import com.google.gson.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.WeightedRandomChestContent;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class WeightedRandomChestContentSerializer implements JsonSerializer<WeightedRandomChestContent>, JsonDeserializer<WeightedRandomChestContent>
{
    @Override
    public WeightedRandomChestContent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "weightedRandomChestContent");

        int weight = JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "weight");
        int genMin = JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "genMin");
        int genMax = JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "genMax");
        ItemStack stack = context.deserialize(jsonObject.get("item"), ItemStack.class);

        return new WeightedRandomChestContent(stack, genMin, genMax, weight);
    }

    @Override
    public JsonElement serialize(WeightedRandomChestContent src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("weight", src.itemWeight);
        jsonObject.addProperty("genMin", src.theMinimumChanceToGenerateItem);
        jsonObject.addProperty("genMax", src.theMaximumChanceToGenerateItem);
        jsonObject.add("item", context.serialize(src.theItemId, ItemStack.class));

        return jsonObject;
    }
}
