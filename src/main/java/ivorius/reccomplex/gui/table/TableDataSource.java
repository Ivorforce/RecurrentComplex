/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

/**
 * Created by lukas on 04.06.14.
 */
public interface TableDataSource
{
    int numberOfElements();

    TableElement elementForIndex(GuiTable table, int index);
}
