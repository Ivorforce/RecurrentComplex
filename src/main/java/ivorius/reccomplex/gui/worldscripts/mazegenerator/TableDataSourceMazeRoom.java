/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import com.google.common.collect.ImmutableList;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.reccomplex.gui.table.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by lukas on 13.04.16.
 */
public class TableDataSourceMazeRoom extends TableDataSourceSegmented
{
    private MazeRoom room;
    private Consumer<MazeRoom> consumer;

    private final List<IntegerRange> ranges;
    private final List<String> titles;

    public TableDataSourceMazeRoom(MazeRoom room, Consumer<MazeRoom> consumer, List<IntegerRange> ranges, List<String> titles)
    {
        if (ranges.size() != titles.size() || ranges.size() != room.getDimensions())
            throw new IllegalArgumentException();

        this.room = room;
        this.consumer = consumer;
        this.ranges = ImmutableList.copyOf(ranges);
        this.titles = ImmutableList.copyOf(titles);
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return room.getDimensions();
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        IntegerRange range = ranges.get(index);
        int val = room.getCoordinate(index);
        String title = titles.get(index);

        if (range != null)
        {
            TableCellInteger cell = new TableCellInteger(null, val, range.min, range.max);
            cell.addPropertyListener(createListener(cell, index));
            return new TableElementCell(title, cell);
        }
        else
        {
            TableCellStringInt cell = new TableCellStringInt(null, val);
            cell.addPropertyListener(createListener(cell, index));
            return new TableElementCell(title, cell);
        }
    }

    @Nonnull
    protected TableCellPropertyListener createListener(TableCellProperty<Integer> cell, int idx)
    {
        return cell1 -> consumer.accept(room = setInDimension(room, idx, (int) cell1.getPropertyValue()));
    }

    private MazeRoom setInDimension(MazeRoom room, int dimension, int value)
    {
        return this.room.addInDimension(dimension, value - room.getCoordinate(dimension));
    }
}
