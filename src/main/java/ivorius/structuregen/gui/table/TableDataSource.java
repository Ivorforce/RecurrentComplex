/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.gui.table;

/**
 * Created by lukas on 04.06.14.
 */
public interface TableDataSource
{
    boolean has(GuiTable table, int index);

    TableElement elementForIndex(GuiTable table, int index);
}
