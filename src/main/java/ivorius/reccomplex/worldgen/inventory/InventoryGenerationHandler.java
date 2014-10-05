/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.items.GeneratingItem;
import ivorius.reccomplex.json.ItemStackSerializer;
import ivorius.reccomplex.json.NbtToJson;
import ivorius.reccomplex.worldgen.MCRegistrySpecial;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandomChestContent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by lukas on 25.05.14.
 */
public class InventoryGenerationHandler
{
    private static Map<String, InventoryGenerator> inventoryGeneratorMap = new HashMap<>();

    private static Gson gson = createGson();

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(GenericInventoryGenerator.class, new GenericInventoryGenerator.Serializer());
        builder.registerTypeAdapter(WeightedRandomChestContent.class, new WeightedRandomChestContentSerializer());
        builder.registerTypeAdapter(ItemStack.class, new ItemStackSerializer(MCRegistrySpecial.INSTANCE));

        NbtToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public static void registerInventoryGenerator(InventoryGenerator inventoryGenerator, String key)
    {
        if (inventoryGenerator.areDependenciesResolved())
        {
            RecurrentComplex.logger.info(inventoryGeneratorMap.containsKey(key) ? "Overwrote inventory generator with id '" + key + "'" : "Registered inventory generator with id '" + key + "'");

            inventoryGeneratorMap.put(key, inventoryGenerator);
        }
    }

    public static void registerInventoryGenerator(ResourceLocation resourceLocation, String key, String modName)
    {
        GenericInventoryGenerator generator = InventoryGeneratorSaveHandler.readInventoryGenerator(resourceLocation);

        if (generator != null)
        {
            ModInventoryGeneratorGeneric generatorMod = new ModInventoryGeneratorGeneric(modName, generator);
            registerInventoryGenerator(generatorMod, key);
        }
    }

    public static InventoryGenerator generator(String key)
    {
        return inventoryGeneratorMap.get(key);
    }

    public static Set<String> allInventoryGeneratorKeys()
    {
        return inventoryGeneratorMap.keySet();
    }

    public static void removeGenerator(String key)
    {
        inventoryGeneratorMap.remove(key);
    }

    public static String createJSONFromInventoryGenerator(GenericInventoryGenerator inventoryGenerator)
    {
        return gson.toJson(inventoryGenerator, GenericInventoryGenerator.class);
    }

    public static GenericInventoryGenerator createInventoryGeneratorFromJSON(String json) throws InventoryLoadException
    {
        try
        {
            return gson.fromJson(json, GenericInventoryGenerator.class);
        }
        catch (JsonSyntaxException e)
        {
            throw new InventoryLoadException(e);
        }
    }

    public static void generateAllTags(IInventory inventory, Random random)
    {
        List<Pair<ItemStack, Integer>> foundGenerators = new ArrayList<>();
        boolean didChange = true;
        int cycles = 0;

        do
        {
            if (didChange)
            {
                for (int i = 0; i < inventory.getSizeInventory(); i++)
                {
                    ItemStack stack = inventory.getStackInSlot(i);

                    if (stack != null && (stack.getItem() instanceof GeneratingItem))
                    {
                        foundGenerators.add(new ImmutablePair<>(stack, i));
                        inventory.setInventorySlotContents(i, null);
                    }
                }

                didChange = false;
            }

            if (foundGenerators.size() > 0)
            {
                Pair<ItemStack, Integer> pair = foundGenerators.get(0);
                ItemStack stack = pair.getLeft();
                ((GeneratingItem) stack.getItem()).generateInInventory(inventory, random, stack, pair.getRight());

                foundGenerators.remove(0);
                didChange = true;
            }

            cycles++;
        }
        while ((foundGenerators.size() > 0 || didChange) && cycles < 1000);
    }
}
