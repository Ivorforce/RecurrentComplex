package ivorius.structuregen.gui;

import net.minecraft.inventory.IInventory;

/**
 * Created by lukas on 27.05.14.
 */
public interface InventoryWatcher
{
    void inventoryChanged(IInventory inventory);
}
