/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

/**
 * Created by lukas on 22.06.14.
 */
public abstract class TableDataSourceSegmented implements TableDataSource
{
    @Override
    public boolean has(GuiTable table, int index)
    {
        if (index < 0)
        {
            return false;
        }

        for (int seg = 0; seg < numberOfSegments(); seg++)
        {
            index -= sizeOfSegment(seg);
        }

        return index < 0;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        for (int seg = 0; seg < numberOfSegments(); seg++)
        {
            if (index - sizeOfSegment(seg) < 0)
            {
                return elementForIndexInSegment(table, index, seg);
            }

            index -= sizeOfSegment(seg);
        }

        return null;
    }

    public abstract int numberOfSegments();

    public abstract int sizeOfSegment(int segment);

    public abstract TableElement elementForIndexInSegment(GuiTable table, int index, int segment);
}
