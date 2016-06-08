/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.maze.ConnectorStrategy;
import ivorius.reccomplex.structures.generic.maze.SavedMazePath;
import ivorius.reccomplex.structures.generic.maze.SavedMazePathConnection;
import net.minecraft.util.EnumFacing;

/**
 * Created by lukas on 22.06.14.
 */
public class TableDataSourceMazePath extends TableDataSourceSegmented implements TableCellPropertyListener, TableCellActionListener
{
    private SavedMazePath mazePath;
    private int[] boundsLower;
    private int[] boundsHigher;
    private TableDelegate tableDelegate;

    private TableCellButton invertableButton;

    public TableDataSourceMazePath(SavedMazePath mazePath, int[] boundsLower, int[] boundsHigher, TableDelegate tableDelegate)
    {
        this.mazePath = mazePath;
        this.boundsLower = boundsLower;
        this.boundsHigher = boundsHigher;
        this.tableDelegate = tableDelegate;
    }

    @Override
    public int numberOfSegments()
    {
        return 4;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
                return boundsLower.length;
            case 2:
                return 1;
            case 3:
                return 1;
            default:
                return super.sizeOfSegment(segment);
        }
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 1)
        {
            String id = "pos" + index;
            String title = String.format("Position: %s", index == 0 ? "X" : index == 1 ? "Y" : index == 2 ? "Z" : "" + index);
            TableCellInteger cell = new TableCellInteger(id, mazePath.sourceRoom.getCoordinates()[index], boundsLower[index], boundsHigher[index]);
            cell.addPropertyListener(this);
            return new TableElementCell(title, cell);
        }
        else if (segment == 2)
        {
            TableCellEnum.Option<EnumFacing>[] optionList = TableDirections.getDirectionOptions(EnumFacing.VALUES);

            TableCellEnum cell = new TableCellEnum<>("side", directionFromPath(mazePath), optionList);
            cell.addPropertyListener(this);
            return new TableElementCell("Side", cell);
        }
        else if (segment == 3)
        {
            invertableButton = new TableCellButton("actions", new TableCellButton.Action("inverse", "Invert", isInvertable()));
            invertableButton.addListener(this);
            return new TableElementCell(invertableButton);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    protected boolean isInvertable()
    {
        return contains(mazePath.inverse().getSourceRoom().getCoordinates(), boundsLower, boundsHigher);
    }

    public static boolean contains(int[] array, int[] lower, int[] higher)
    {
        for (int i = 0; i < array.length; i++)
        {
            if (array[i] < lower[i] || array[i] > higher[i])
                return false;
        }

        return true;
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("side".equals(cell.getID()))
        {
            SavedMazePathConnection path = pathFromDirection((EnumFacing) cell.getPropertyValue(), mazePath.sourceRoom.getCoordinates());
            mazePath.pathDimension = path.path.pathDimension;
            mazePath.pathGoesUp = path.path.pathGoesUp;
            tableDelegate.reloadData();
        }
        else if (cell.getID() != null)
        {
            int index = Integer.valueOf(cell.getID().substring(3));
            mazePath.sourceRoom = mazePath.sourceRoom.addInDimension(index, (int) cell.getPropertyValue() - mazePath.sourceRoom.getCoordinate(index));

            if (invertableButton != null)
                invertableButton.setEnabled("inverse", isInvertable());
        }
    }

    public static EnumFacing directionFromPath(SavedMazePath path)
    {
        switch (path.pathDimension)
        {
            case 0:
                return path.pathGoesUp ? EnumFacing.EAST : EnumFacing.WEST;
            case 1:
                return path.pathGoesUp ? EnumFacing.UP : EnumFacing.DOWN;
            case 2:
                return path.pathGoesUp ? EnumFacing.SOUTH : EnumFacing.NORTH;
        }

        return null;
    }

    public static SavedMazePathConnection pathFromDirection(EnumFacing side, int[] room)
    {
        int pathDim = side.getFrontOffsetX() != 0 ? 0 : side.getFrontOffsetY() != 0 ? 1 : side.getFrontOffsetZ() != 0 ? 2 : -1;
        int offset = side.getFrontOffsetX() + side.getFrontOffsetY() + side.getFrontOffsetZ();

        return new SavedMazePathConnection(pathDim, new MazeRoom(room[0], room[1], room[2]), offset > 0, ConnectorStrategy.DEFAULT_PATH);
    }

    @Override
    public void actionPerformed(TableCell cell, String action)
    {
        if ("inverse".equals(action))
        {
            mazePath.set(mazePath.inverse());
            tableDelegate.reloadData();
        }
    }
}
