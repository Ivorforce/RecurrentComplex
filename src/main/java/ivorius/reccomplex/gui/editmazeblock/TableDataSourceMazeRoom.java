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
public class TableDataSourceMazeRoom implements TableDataSource, TableElementPropertyListener
{
    private MazeRoom room;
    private int[] dimensions;

    public TableDataSourceMazeRoom(MazeRoom room, int[] dimensions)
    {
        this.room = room;
        this.dimensions = dimensions;
    }

    @Override
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index < dimensions.length;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        String id = "pos" + index;
        String title = String.format("Position: %s", index == 0 ? "X" : index == 1 ? "Y" : index == 2 ? "Z" : "" + index);
        TableElementInteger element = new TableElementInteger(id, title, room.coordinates[index], 0, dimensions[index] - 1);
        element.addPropertyListener(this);

        return element;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        int index = Integer.valueOf(element.getID().substring(3));
        room.coordinates[index] = (int) element.getPropertyValue();
    }
}
