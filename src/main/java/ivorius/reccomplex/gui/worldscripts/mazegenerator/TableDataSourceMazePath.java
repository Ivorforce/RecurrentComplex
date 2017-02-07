/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.cell.TableCellEnum;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.ConnectorStrategy;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePath;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedMazePathConnection;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 22.06.14.
 */
public class TableDataSourceMazePath extends TableDataSourceSegmented
{
    public static final String[] COORD_NAMES = {"x", "y", "z"};

    private SavedMazePath mazePath;
    private List<IntegerRange> bounds;
    private TableDelegate tableDelegate;

    private TableCellButton invertableButton;

    public TableDataSourceMazePath(SavedMazePath mazePath, List<IntegerRange> bounds, TableDelegate tableDelegate)
    {
        this.mazePath = mazePath;
        this.bounds = bounds;
        this.tableDelegate = tableDelegate;

        addManagedSegment(1, new TableDataSourceMazeRoom(mazePath.sourceRoom, mazeRoom -> mazePath.sourceRoom = mazeRoom,bounds,
                Arrays.stream(COORD_NAMES).map(s -> IvTranslations.get("reccomplex.generationInfo.mazeComponent.position." + s)).collect(Collectors.toList()),
                Arrays.stream(COORD_NAMES).map(s -> IvTranslations.getLines("reccomplex.generationInfo.mazeComponent.position." + s + ".tooltip")).collect(Collectors.toList()))
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

    @Nonnull
    @Override
    public String title()
    {
        return "Path";
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
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 2)
        {
            TableCellEnum.Option<EnumFacing>[] optionList = TableDirections.getDirectionOptions(EnumFacing.VALUES);

            TableCellEnum<EnumFacing> cell = new TableCellEnum<>("side", directionFromPath(mazePath), optionList);
            cell.addPropertyConsumer(val -> {
                SavedMazePathConnection path = pathFromDirection(val, mazePath.sourceRoom.getCoordinates());
                mazePath.pathDimension = path.path.pathDimension;
                mazePath.pathGoesUp = path.path.pathGoesUp;
                tableDelegate.reloadData();
            });
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.mazeComponent.path.side"), cell);
        }
        else if (segment == 3)
        {
            invertableButton = new TableCellButton("actions", "inverse", IvTranslations.get("reccomplex.generationInfo.mazeComponent.path.invert"), isInvertable());
            invertableButton.addAction(() -> {
                mazePath.set(mazePath.inverse());
                tableDelegate.reloadData();
            });
            return new TitledCell(invertableButton);
        }

        return super.cellForIndexInSegment(table, index, segment);
    }

    protected boolean isInvertable()
    {
        return contains(mazePath.inverse().getSourceRoom().getCoordinates(), bounds);
    }
}
