/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import ivorius.reccomplex.json.JsonUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.conditions.LootCondition;

import java.util.Collection;
import java.util.Random;

/**
 * Created by lukas on 04.10.16.
 */
public class LootEntryItemCollection extends LootEntry
{
    public String itemCollectionID;

    public LootEntryItemCollection(int weightIn, int qualityIn, LootCondition[] conditionsIn, String entryName, String itemCollectionID)
    {
        super(weightIn, qualityIn, conditionsIn, entryName);
        this.itemCollectionID = itemCollectionID;
    }

    public static LootEntryItemCollection deserialize(int weightIn, int qualityIn, LootCondition[] conditionsIn, String entryName, JsonObject json, JsonDeserializationContext context)
    {
        return new LootEntryItemCollection(weightIn, qualityIn, conditionsIn, entryName,
                JsonUtils.getString(json, "itemCollectionID", ""));
    }

    @Override
    public void addLoot(Collection<ItemStack> stacks, Random rand, LootContext context)
    {
        GenericItemCollection.Component component = GenericItemCollectionRegistry.INSTANCE.get(itemCollectionID);
        if (component != null)
            stacks.add(component.getRandomItemStack(rand));
    }

    @Override
    protected void serialize(JsonObject json, JsonSerializationContext context)
    {
        json.addProperty("itemCollectionID", itemCollectionID);
    }
}
