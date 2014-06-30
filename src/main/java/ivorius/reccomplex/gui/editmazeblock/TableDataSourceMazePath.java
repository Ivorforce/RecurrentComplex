/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.reccomplex.gui.table.*;
import ivorius.ivtoolkit.maze.MazePath;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 22.06.14.
 */
public class TableDataSourceMazePath implements TableDataSource, TableElementPropertyListener
{
    private MazePath mazePath;

    public TableDataSourceMazePath(MazePath mazePath)
    {
        this.mazePath = mazePath;
    }

    @Override
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index <= 3;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index >= 0 && index < 3)
        {
            String id = "pos" + index;
            String title = index == 0 ? "Position: X" : index == 1 ? "Position: Y" : "Position: Z";
            TableElementInteger element = new TableElementInteger(id, title, mazePath.sourceRoom.coordinates[index], 0, 20);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 3)
        {
            List<TableElementList.Option> optionList = new ArrayList<>();
            optionList.add(new TableElementList.Option("" + 0, "Down (-Y)"));
            optionList.add(new TableElementList.Option("" + 1, "Up (+Y)"));
            optionList.add(new TableElementList.Option("" + 2, "North (-Z)"));
            optionList.add(new TableElementList.Option("" + 3, "South (+Z)"));
            optionList.add(new TableElementList.Option("" + 4, "West (-X)"));
            optionList.add(new TableElementList.Option("" + 5, "East (+X)"));

            TableElementList element = new TableElementList("side", "Side", directionFromPath(mazePath).ordinal() + "", optionList);
            element.addPropertyListener(this);
            return element;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("side".equals(element.getID()))
        {
            int side = Integer.valueOf(((String) element.getPropertyValue()));
            MazePath path = pathFromDirection(ForgeDirection.values()[side], mazePath.sourceRoom.coordinates);
            mazePath.pathDimension = path.pathDimension;
            mazePath.pathGoesUp = path.pathGoesUp;
        }
        else
        {
            int index = Integer.valueOf(element.getID().substring(3));
            mazePath.sourceRoom.coordinates[index] = (int) element.getPropertyValue();
        }
    }

    private static ForgeDirection directionFromPath(MazePath path)
    {
        switch (path.pathDimension)
        {
            case 0:
                return path.pathGoesUp ? ForgeDirection.EAST : ForgeDirection.WEST;
            case 1:
                return path.pathGoesUp ? ForgeDirection.UP : ForgeDirection.DOWN;
            case 2:
                return path.pathGoesUp ? ForgeDirection.SOUTH : ForgeDirection.NORTH;
        }

        return null;
    }

    private static MazePath pathFromDirection(ForgeDirection side, int[] room)
    {
        int pathDim = side.offsetX != 0 ? 0 : side.offsetY != 0 ? 1 : side.offsetZ != 0 ? 2 : -1;
        int offset = side.offsetX + side.offsetY + side.offsetZ;

        return new MazePath(pathDim, offset > 0, room[0], room[1], room[2]);
    }
}
