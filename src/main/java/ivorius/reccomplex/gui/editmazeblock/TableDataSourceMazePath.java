/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.maze.ConnectorStrategy;
import ivorius.reccomplex.structures.generic.maze.SavedMazePath;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by lukas on 22.06.14.
 */
public class TableDataSourceMazePath extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private SavedMazePath mazePath;
    private int[] boundsLower;
    private int[] boundsHigher;

    public TableDataSourceMazePath(SavedMazePath mazePath, int[] boundsLower, int[] boundsHigher)
    {
        this.mazePath = mazePath;
        this.boundsLower = boundsLower;
        this.boundsHigher = boundsHigher;
        addManagedSection(0, new TableDataSourceConnector(mazePath.connector, IvTranslations.get("reccomplex.maze.connector")));
    }

    @Override
    public int numberOfSegments()
    {
        return 3;
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
            TableCellEnum.Option<ForgeDirection>[] optionList = TableDirections.getDirectionOptions(ForgeDirection.VALID_DIRECTIONS);

            TableCellEnum cell = new TableCellEnum<>("side", directionFromPath(mazePath), optionList);
            cell.addPropertyListener(this);
            return new TableElementCell("Side", cell);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("side".equals(cell.getID()))
        {
            SavedMazePath path = pathFromDirection((ForgeDirection) cell.getPropertyValue(), mazePath.sourceRoom.getCoordinates());
            mazePath.pathDimension = path.pathDimension;
            mazePath.pathGoesUp = path.pathGoesUp;
        }
        else if (cell.getID() != null)
        {
            int index = Integer.valueOf(cell.getID().substring(3));
            mazePath.sourceRoom = mazePath.sourceRoom.addInDimension(index, (int) cell.getPropertyValue() - mazePath.sourceRoom.getCoordinate(index));;
        }
    }

    public static ForgeDirection directionFromPath(SavedMazePath path)
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

    public static SavedMazePath pathFromDirection(ForgeDirection side, int[] room)
    {
        int pathDim = side.offsetX != 0 ? 0 : side.offsetY != 0 ? 1 : side.offsetZ != 0 ? 2 : -1;
        int offset = side.offsetX + side.offsetY + side.offsetZ;

        return new SavedMazePath(pathDim, new MazeRoom(room[0], room[1], room[2]), offset > 0, ConnectorStrategy.DEFAULT_PATH);
    }
}
