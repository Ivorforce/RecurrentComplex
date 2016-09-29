/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.gson.*;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.json.ItemStackSerializer;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.json.NbtToJson;
import ivorius.reccomplex.structures.generic.matchers.DependencyMatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.WorldServer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class GenericItemCollection implements WeightedItemCollection
{
    public static final int LATEST_VERSION = 2;

    private static Gson gson = createGson();

    public final List<Component> components = new ArrayList<>();

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(Component.class, new Component.Serializer());
        builder.registerTypeAdapter(RandomizedItemStack.class, new RandomizedItemStack.Serializer());
        builder.registerTypeAdapter(ItemStack.class, new ItemStackSerializer(RecurrentComplex.specialRegistry));
        builder.registerTypeAdapter(WeightedRandomChestContent.class, new WeightedRandomChestContentSerializer());
        NbtToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    @Override
    public ItemStack getRandomItemStack(WorldServer server, Random random)
    {
        int max = 0;
        for (Component component : components)
        {
            component.updateWeightCache();
            max += component.itemWeight;
        }

        if (max > 0)
            return WeightedRandom.getRandomItem(random, components, max).getRandomItemStack(random);

        return null;
    }

    @Override
    public String getDescriptor()
    {
        return IvTranslations.get("inventoryGen.custom");
    }

    public static class Component extends WeightedRandom.Item
    {
        public final List<RandomizedItemStack> items = new ArrayList<>();
        public final DependencyMatcher dependencies = new DependencyMatcher("");
        public String inventoryGeneratorID;

        public Component()
        {
            super(0);
            inventoryGeneratorID = "";
        }

        public Component(String inventoryGeneratorID, List<RandomizedItemStack> items, String dependencies)
        {
            super(0);
            this.inventoryGeneratorID = inventoryGeneratorID;
            this.items.addAll(items);
            this.dependencies.setExpression(dependencies);
        }

        public static Component createDefaultComponent()
        {
            return new Component();
        }

        public ItemStack getRandomItemStack(Random random)
        {
            if (items.size() == 0)
                return null;

            RandomizedItemStack item = WeightedSelector.selectItem(random, items);

            return item.generateStack(random);

        }

        public boolean areDependenciesResolved()
        {
            return dependencies.getAsBoolean();
        }

        public void updateWeightCache()
        {
            itemWeight = items.size();
        }

        public Component copy()
        {
            try
            {
                return ItemCollectionSaveHandler.INSTANCE.fromJSON(ItemCollectionSaveHandler.INSTANCE.toJSON(this));
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
                JsonObject jsonObject = JsonUtils.asJsonObject(jsonElement, "status");

                int version;
                if (jsonObject.has("version"))
                {
                    version = JsonUtils.getInt(jsonObject, "version");
                }
                else
                {
                    version = LATEST_VERSION;
                    RecurrentComplex.logger.warn("InventoryGen JSON missing 'version', using latest (" + LATEST_VERSION + ")");
                }

                String generatorID = JsonUtils.getString(jsonObject, "inventoryGeneratorID", "");

                List<RandomizedItemStack> stacks = new ArrayList<>();

                if (jsonObject.has("items"))
                    stacks.addAll(Lists.newArrayList(gson.fromJson(jsonObject.get("items"), RandomizedItemStack[].class)));

                if (version == 1 && jsonObject.has("contents")) // Legacy
                {
                    List<WeightedRandomChestContent> chestContents = Lists.newArrayList(gson.fromJson(jsonObject.get("contents"), WeightedRandomChestContent[].class));
                    stacks.addAll(Collections2.transform(chestContents, input -> RandomizedItemStack.from(input, 100)));
                }

                String dependencyExpression = JsonUtils.getString(jsonObject, "dependencyExpression", "");
                if (version == 1 && jsonObject.has("dependencies")) // Legacy
                {
                    String[] dependencies = context.deserialize(jsonObject.get("dependencies"), String[].class);
                    dependencyExpression = DependencyMatcher.ofMods(dependencies);
                }

                return new Component(generatorID, stacks, dependencyExpression);
            }

            public JsonElement serialize(Component src, Type par2Type, JsonSerializationContext context)
            {
                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("version", LATEST_VERSION);

                jsonObject.addProperty("inventoryGeneratorID", src.inventoryGeneratorID);

                jsonObject.add("items", gson.toJsonTree(src.items));

                jsonObject.add("dependencyExpression", context.serialize(src.dependencies.getExpression()));

                return jsonObject;
            }
        }
    }

    public static class RandomizedItemStack implements WeightedSelector.Item
    {
        public ItemStack itemStack;

        public int min;
        public int max;

        public double weight;

        public RandomizedItemStack(ItemStack itemStack, int min, int max, double weight)
        {
            this.itemStack = itemStack;
            this.min = min;
            this.max = max;
            this.weight = weight;
        }

        public static RandomizedItemStack from(WeightedRandomChestContent chestContent, int defaultWeight)
        {
            return new RandomizedItemStack(chestContent.theItemId.copy(),
                    chestContent.minStackSize, chestContent.maxStackSize,
                    (double) chestContent.itemWeight / (double) defaultWeight);
        }

        public ItemStack generateStack(Random random)
        {
            // safety
            int min = Math.min(this.max, this.min);
            int max = Math.max(this.max, this.min);

            ItemStack stack = itemStack.copy();
            stack.stackSize = random.nextInt(max - min + 1) + min;
            return stack;
        }

        @Override
        public double getWeight()
        {
            return weight;
        }

        public static class Serializer implements JsonDeserializer<RandomizedItemStack>, JsonSerializer<RandomizedItemStack>
        {
            @Override
            public RandomizedItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                JsonObject jsonObject = JsonUtils.asJsonObject(json, "randomizedItem");

                double weight = JsonUtils.getDouble(jsonObject, "weight");
                int min = JsonUtils.getInt(jsonObject, "min");
                int max = JsonUtils.getInt(jsonObject, "max");
                ItemStack stack = context.deserialize(jsonObject.get("item"), ItemStack.class);

                return new RandomizedItemStack(stack, min, max, weight);
            }

            @Override
            public JsonElement serialize(RandomizedItemStack src, Type typeOfSrc, JsonSerializationContext context)
            {
                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("weight", src.weight);
                jsonObject.addProperty("min", src.min);
                jsonObject.addProperty("max", src.max);
                jsonObject.add("item", context.serialize(src.itemStack));

                return jsonObject;
            }
        }
    }
}
