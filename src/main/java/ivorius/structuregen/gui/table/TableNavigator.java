/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.gui.table;

import java.util.Stack;

/**
 * Created by lukas on 05.06.14.
 */
public interface TableNavigator
{
    GuiTable popTable();

    void pushTable(GuiTable table);

    void setTable(GuiTable table);

    GuiTable currentTable();

    Stack<GuiTable> tableStack();
}
