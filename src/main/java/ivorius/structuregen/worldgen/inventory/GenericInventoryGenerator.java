package ivorius.structuregen.worldgen.inventory;

import com.google.gson.*;
import cpw.mods.fml.common.Loader;
import ivorius.structuregen.StructureGen;
import ivorius.structuregen.gui.SGGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.StatCollector;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class GenericInventoryGenerator implements InventoryGenerator, Cloneable
{
    public static final int LATEST_VERSION = 1;

    public int minItems;
    public int maxItems;

    public List<WeightedRandomChestContent> weightedRandomChestContents;

    public List<String> dependencies = new ArrayList<>();

    public GenericInventoryGenerator(int minItems, int maxItems, List<String> dependencies, WeightedRandomChestContent... weightedRandomChestContents)
    {
        this.minItems = minItems;
        this.maxItems = maxItems;
        this.dependencies = new ArrayList<>();
        this.dependencies.addAll(dependencies);
        this.weightedRandomChestContents = new ArrayList<>();
        Collections.addAll(this.weightedRandomChestContents, weightedRandomChestContents);
    }

    public GenericInventoryGenerator(int minItems, int maxItems, List<String> dependencies, List<WeightedRandomChestContent> weightedRandomChestContents)
    {
        this.minItems = minItems;
        this.maxItems = maxItems;
        this.dependencies = new ArrayList<>();
        this.dependencies.addAll(dependencies);
        this.weightedRandomChestContents = new ArrayList<>();
        this.weightedRandomChestContents.addAll(weightedRandomChestContents);
    }

    public static GenericInventoryGenerator createDefaultGenerator()
    {
        return new GenericInventoryGenerator(4, 8, Collections.<String>emptyList());
    }

    @Override
    public void generateInInventory(Random random, IInventory inventory)
    {
        int number = minItems + random.nextInt(maxItems - minItems + 1);

        WeightedRandomChestContent[] weightedRandomChestContents = this.weightedRandomChestContents.toArray(new WeightedRandomChestContent[this.weightedRandomChestContents.size()]);
        WeightedRandomChestContent.generateChestContents(random, weightedRandomChestContents, inventory, number);
    }

    @Override
    public void generateInInventorySlot(Random random, IInventory inventory, int slot)
    {
        WeightedRandomChestContent item = (WeightedRandomChestContent) WeightedRandom.getRandomItem(random, weightedRandomChestContents);
        ItemStack[] stacks = ChestGenHooks.generateStacks(random, item.theItemId, item.theMinimumChanceToGenerateItem, item.theMaximumChanceToGenerateItem);
        inventory.setInventorySlotContents(slot, stacks.length > 0 ? stacks[0] : null);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedInfo)
    {
        list.add(StatCollector.translateToLocal("inventoryGen.custom"));
    }

    @Override
    public boolean openEditGUI(ItemStack stack, EntityPlayer player, int slot)
    {
        player.openGui(StructureGen.instance, SGGuiHandler.editInventoryGen, player.getEntityWorld(), slot, 0, 0);

        return true;
    }

    @Override
    public GenericInventoryGenerator copyAsGenericInventoryGenerator()
    {
        return (GenericInventoryGenerator) clone();
    }

    @Override
    public boolean areDependenciesResolved()
    {
        for (String mod : dependencies)
        {
            if (!Loader.isModLoaded(mod))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public Object clone()
    {
        return InventoryGenerationHandler.createInventoryGeneratorFromJSON(InventoryGenerationHandler.createJSONFromInventoryGenerator(this));
    }

    public static class Serializer implements JsonDeserializer<GenericInventoryGenerator>, JsonSerializer<GenericInventoryGenerator>
    {
        public GenericInventoryGenerator deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "status");

            Integer version;
            if (jsonobject.has("version"))
            {
                version = JsonUtils.getJsonObjectIntegerFieldValue(jsonobject, "version");
            }
            else
            {
                version = LATEST_VERSION;
                StructureGen.logger.warn("InventoryGen JSON missing 'version', using latest (" + getClass() + ")");
            }

            int minItems = JsonUtils.getJsonObjectIntegerFieldValue(jsonobject, "minItems");
            int maxItems = JsonUtils.getJsonObjectIntegerFieldValue(jsonobject, "maxItems");
            JsonArray chestContentsJSON = JsonUtils.getJsonObjectJsonArrayField(jsonobject, "contents");
            List<WeightedRandomChestContent> chestContents = new ArrayList<WeightedRandomChestContent>();

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

            return new GenericInventoryGenerator(minItems, maxItems, dependencies, chestContents);
        }

        public JsonElement serialize(GenericInventoryGenerator structureInfo, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("version", LATEST_VERSION);

            jsonobject.addProperty("minItems", structureInfo.minItems);
            jsonobject.addProperty("maxItems", structureInfo.maxItems);
            JsonArray chestContentsJSON = new JsonArray();
            for (WeightedRandomChestContent weightedRandomChestContent : structureInfo.weightedRandomChestContents)
            {
                chestContentsJSON.add(context.serialize(weightedRandomChestContent, WeightedRandomChestContent.class));
            }
            jsonobject.add("contents", chestContentsJSON);

            if (structureInfo.dependencies.size() > 0)
            {
                JsonArray dependencyArray = new JsonArray();
                for (String s : structureInfo.dependencies)
                {
                    dependencyArray.add(context.serialize(s));
                }
                jsonobject.add("dependencies", dependencyArray);
            }

            return jsonobject;
        }
    }
}
