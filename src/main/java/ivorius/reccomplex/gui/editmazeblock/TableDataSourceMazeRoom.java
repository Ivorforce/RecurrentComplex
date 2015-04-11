/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.ivtoolkit.maze.MazeRoom;
import ivorius.reccomplex.gui.table.*;

/**
 * Created by lukas on 22.06.14.
 */
public class TableDataSourceMazeRoom extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private MazeRoom room;
    private int[] dimensions;

    public TableDataSourceMazeRoom(MazeRoom room, int[] dimensions)
    {
        this.room = room;
        this.dimensions = dimensions;
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return dimensions.length;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        String id = "pos" + index;
        String title = String.format("Position: %s", index == 0 ? "X" : index == 1 ? "Y" : index == 2 ? "Z" : "" + index);
        TableCellInteger cell = new TableCellInteger(id, room.coordinates[index], 0, dimensions[index] - 1);
        cell.addPropertyListener(this);

        return new TableElementCell(title, cell);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if (cell.getID() != null)
        {
            int index = Integer.valueOf(cell.getID().substring(3));
            room.coordinates[index] = (int) cell.getPropertyValue();
        }
    }
}
