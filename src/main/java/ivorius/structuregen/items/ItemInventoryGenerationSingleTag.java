package ivorius.structuregen.items;

import ivorius.structuregen.worldgen.inventory.InventoryGenerator;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.Random;

public class ItemInventoryGenerationSingleTag extends ItemInventoryGenerationTag
{
    @Override
    public void generateInInventory(IInventory inventory, Random random, ItemStack stack, int fromSlot)
    {
        InventoryGenerator inventoryGenerator = inventoryGenerator(stack);

        if (inventoryGenerator != null)
        {
            inventoryGenerator.generateInInventorySlot(random, inventory, fromSlot);
        }
    }
}
