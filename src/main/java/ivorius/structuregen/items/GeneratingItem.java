package ivorius.structuregen.items;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public interface GeneratingItem
{
    void generateInInventory(IInventory inventory, Random random, ItemStack stack, int fromSlot);
}
