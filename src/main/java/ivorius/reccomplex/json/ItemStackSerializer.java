/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.json;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack>
{
    private MCRegistry registry;

    public ItemStackSerializer(MCRegistry registry)
    {
        this.registry = registry;
    }

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", Item.itemRegistry.getNameForObject(src.getItem()));
        jsonObject.addProperty("damage", src.getItemDamage());
        jsonObject.addProperty("count", src.stackSize);

        if (src.hasTagCompound())
        {
            if (RecurrentComplex.USE_JSON_FOR_NBT)
            {
                jsonObject.add("tag", context.serialize(src.getTagCompound()));
            }
            else
            {
                jsonObject.addProperty("tagBase64", NbtToJson.getBase64FromNBT(src.getTagCompound()));
            }
        }

        return jsonObject;
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "ItemStack");

        String id = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "id");
        Item item = registry.itemFromID(id);
        int damage = JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "damage");
        int count = JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "count");

        ItemStack stack = new ItemStack(item, count, damage);

        if (jsonObject.has("tag"))
        {
            NBTTagCompound compound = context.deserialize(jsonObject.get("tag"), NBTTagCompound.class);
            stack.setTagCompound(compound);
        }
        else if (jsonObject.has("tagBase64"))
        {
            NBTTagCompound compound = NbtToJson.getNBTFromBase64(JsonUtils.getJsonObjectStringFieldValue(jsonObject, "tagBase64"));
            stack.setTagCompound(compound);
        }

        return stack;
    }
}
