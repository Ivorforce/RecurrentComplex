/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.reccomplex.gui.table.TableDataSourceSegmented;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Created by lukas on 17.01.15.
 */
public class TableDataSourceItem<T extends Item> extends TableDataSourceSegmented
{
    public ItemStack stack;
    public T item;

    public void setStack(ItemStack stack)
    {
        this.stack = stack;
        this.item = (T) stack.getItem();
    }
}
