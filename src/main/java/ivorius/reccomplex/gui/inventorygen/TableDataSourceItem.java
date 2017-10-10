/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 17.01.15.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceItem<T extends Item> extends TableDataSourceSegmented
{
    public ItemStack stack;
    public T item;

    public void setStack(ItemStack stack)
    {
        this.stack = stack;
        //noinspection unchecked
        this.item = (T) stack.getItem();
    }
}
