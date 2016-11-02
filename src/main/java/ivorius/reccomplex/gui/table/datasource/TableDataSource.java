/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.datasource;

import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.cell.TableCell;

/**
 * Created by lukas on 04.06.14.
 */
public interface TableDataSource
{
    int numberOfCells();

    TableCell cellForIndex(GuiTable table, int index);
}
