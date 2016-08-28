/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.maze.ConnectorStrategy;
import ivorius.reccomplex.structures.generic.maze.SavedMazePath;
import ivorius.reccomplex.structures.generic.maze.SavedMazePathConnection;
import ivorius.reccomplex.utils.MCMazes;
import net.minecraft.util.EnumFacing;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 22.06.14.
 */
public class TableDataSourceMazePath extends TableDataSourceSegmented implements TableCellPropertyListener, TableCellActionListener
{
    private SavedMazePath mazePath;
    private List<IntegerRange> bounds;
    private TableDelegate tableDelegate;

    private TableCellButton invertableButton;

    public TableDataSourceMazePath(SavedMazePath mazePath, List<IntegerRange> bounds, TableDelegate tableDelegate)
    {
        this.mazePath = mazePath;
        this.bounds = bounds;
        this.tableDelegate = tableDelegate;

        addManagedSection(1, new TableDataSourceMazeRoom(mazePath.sourceRoom, mazeRoom -> mazePath.sourceRoom = mazeRoom,
                bounds, Arrays.stream(new String[]{"x", "y", "z"}).map(s -> IvTranslations.get("reccomplex.generationInfo.mazeComponent.position." + s)).collect(Collectors.toList()))
        );
    }

    public static boolean contains(int[] array, List<IntegerRange> bounds)
    {
        for (int i = 0; i < array.length; i++)
        {
            if (array[i] < bounds.get(i).getMin() || array[i] > bounds.get(i).getMax())
                return false;
        }

        return true;
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
    public int numberOfSegments()
    {
        return 4;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
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
        if (segment == 2)
        {
            TableCellEnum.Option<EnumFacing>[] optionList = TableDirections.getDirectionOptions(EnumFacing.VALUES);

            TableCellEnum cell = new TableCellEnum<>("side", directionFromPath(mazePath), optionList);
            cell.addPropertyListener(this);
            return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.mazeComponent.path.side"), cell);
        }
        else if (segment == 3)
        {
            invertableButton = new TableCellButton("actions", "inverse", IvTranslations.get("reccomplex.generationInfo.mazeComponent.path.invert"), isInvertable());
            invertableButton.addListener(this);
            return new TableElementCell(invertableButton);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    protected boolean isInvertable()
    {
        return contains(mazePath.inverse().getSourceRoom().getCoordinates(), bounds);
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
                invertableButton.setEnabled(isInvertable());
        }
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
