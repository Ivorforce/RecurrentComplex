/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.datasource;

import ivorius.reccomplex.gui.GuiHider;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.cell.TableCell;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 04.06.14.
 */
public interface TableDataSource
{
    @Nonnull
    default String title()
    {
        return "";
    }

    int numberOfCells();

    TableCell cellForIndex(GuiTable table, int index);

    default boolean canVisualize()
    {
        return false;
    }

    default GuiHider.Visualizer visualizer()
    {
        return null;
    }
}
