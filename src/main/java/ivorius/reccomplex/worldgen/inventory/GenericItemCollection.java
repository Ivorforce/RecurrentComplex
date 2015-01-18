/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import com.google.gson.*;
import cpw.mods.fml.common.Loader;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.RCGuiHandler;
import ivorius.reccomplex.json.JsonUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class GenericItemCollection implements WeightedItemCollection
{
    public static final int LATEST_VERSION = 1;

    public final List<Component> components = new ArrayList<>();

    @Override
    public ItemStack getRandomItemStack(Random random)
    {
        int max = 0;
        for (Component component : components)
        {
            component.updateWeightCache();
            max += component.itemWeight;
        }

        if (max > 0)
            return ((Component) WeightedRandom.getRandomItem(random, components, max)).getRandomItemStack(random);

        return null;
    }

    @Override
    public String getDescriptor()
    {
        return StatCollector.translateToLocal("inventoryGen.custom");
    }

    public static class Component extends WeightedRandom.Item
    {
        public String inventoryGeneratorID;

        public final List<WeightedRandomChestContent> items = new ArrayList<>();
        public final List<String> dependencies = new ArrayList<>();

        public Component()
        {
            super(0);
            inventoryGeneratorID = "";
        }

        public Component(String inventoryGeneratorID, List<WeightedRandomChestContent> items, List<String> dependencies)
        {
            super(0);
            this.inventoryGeneratorID = inventoryGeneratorID;
            this.items.addAll(items);
            this.dependencies.addAll(dependencies);
        }

        public static Component createDefaultComponent()
        {
            return new Component();
        }

        public ItemStack getRandomItemStack(Random random)
        {
            if (items.size() == 0)
                return null;

            WeightedRandomChestContent item = (WeightedRandomChestContent) WeightedRandom.getRandomItem(random, items);

            ItemStack[] stacks = ChestGenHooks.generateStacks(random, item.theItemId, item.theMinimumChanceToGenerateItem, item.theMaximumChanceToGenerateItem);
            return stacks.length > 0 ? stacks[0] : null;

        }

        public boolean areDependenciesResolved()
        {
            for (String mod : dependencies)
            {
                if (!Loader.isModLoaded(mod))
                    return false;
            }

            return true;
        }

        public void updateWeightCache()
        {
            itemWeight = items.size();
        }

        public Component copy()
        {
            try
            {
                return GenericItemCollectionRegistry.createComponentFromJSON(GenericItemCollectionRegistry.createJSONFromComponent(this));
            }
            catch (InventoryLoadException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        public static class Serializer implements JsonDeserializer<Component>, JsonSerializer<Component>
        {
            public Component deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
            {
                JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "status");

                int version;
                if (jsonobject.has("version"))
                {
                    version = JsonUtils.getJsonObjectIntegerFieldValue(jsonobject, "version");
                }
                else
                {
                    version = LATEST_VERSION;
                    RecurrentComplex.logger.warn("InventoryGen JSON missing 'version', using latest (" + LATEST_VERSION + ")");
                }

                String generatorID = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonobject, "inventoryGeneratorID", "");

                JsonArray chestContentsJSON = JsonUtils.getJsonObjectJsonArrayField(jsonobject, "contents");
                List<WeightedRandomChestContent> chestContents = new ArrayList<>();

                for (JsonElement element : chestContentsJSON)
                {
                    WeightedRandomChestContent weightedRandomChestContent = context.deserialize(element, WeightedRandomChestContent.class);
                    chestContents.add(weightedRandomChestContent);
                }

                List<String> dependencies = new ArrayList<>();
                if (jsonobject.has("dependencies"))
                {
                    JsonArray dependencyArray = JsonUtils.getJsonObjectJsonArrayField(jsonobject, "dependencies");
                    for (JsonElement element : dependencyArray)
                    {
                        dependencies.add(JsonUtils.getJsonElementStringValue(element, "dependency"));
                    }
                }

                return new Component(generatorID, chestContents, dependencies);
            }

            public JsonElement serialize(Component component, Type par2Type, JsonSerializationContext context)
            {
                JsonObject jsonobject = new JsonObject();

                jsonobject.addProperty("version", LATEST_VERSION);

                jsonobject.addProperty("inventoryGeneratorID", component.inventoryGeneratorID);

                JsonArray chestContentsJSON = new JsonArray();
                for (WeightedRandomChestContent weightedRandomChestContent : component.items)
                {
                    chestContentsJSON.add(context.serialize(weightedRandomChestContent, WeightedRandomChestContent.class));
                }
                jsonobject.add("contents", chestContentsJSON);

                if (component.dependencies.size() > 0)
                {
                    JsonArray dependencyArray = new JsonArray();
                    for (String s : component.dependencies)
                    {
                        dependencyArray.add(context.serialize(s));
                    }
                    jsonobject.add("dependencies", dependencyArray);
                }

                return jsonobject;
            }
        }
    }
}
