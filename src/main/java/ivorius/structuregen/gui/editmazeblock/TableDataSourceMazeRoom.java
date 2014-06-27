/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.gui.editmazeblock;

import ivorius.structuregen.gui.table.*;
import ivorius.ivtoolkit.maze.MazeRoom;

/**
 * Created by lukas on 22.06.14.
 */
public class TableDataSourceMazeRoom implements TableDataSource, TableElementPropertyListener
{
    private MazeRoom room;

    public TableDataSourceMazeRoom(MazeRoom room)
    {
        this.room = room;
    }

    @Override
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index <= 2;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        String id = "pos" + index;
        String title = index == 0 ? "Position: X" : index == 1 ? "Position: Y" : "Position: Z";
        TableElementInteger element = new TableElementInteger(id, title, room.coordinates[index], 0, 20);
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
