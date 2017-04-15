/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import com.google.common.collect.ImmutableList;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellInteger;
import ivorius.reccomplex.gui.table.cell.TableCellStringInt;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by lukas on 13.04.16.
 */
public class TableDataSourceMazeRoom extends TableDataSourceSegmented
{
    public MazeRoom room;
    private Consumer<MazeRoom> consumer;

    private final List<IntegerRange> ranges;
    private final List<String> titles;
    private final List<List<String>> tooltips;

    public TableDataSourceMazeRoom(MazeRoom room, Consumer<MazeRoom> consumer, List<IntegerRange> ranges, List<String> titles, List<List<String>> tooltips)
    {
        if (ranges.size() != titles.size() || ranges.size() != room.getDimensions())
            throw new IllegalArgumentException();

        this.room = room;
        this.consumer = consumer;
        this.ranges = ImmutableList.copyOf(ranges);
        this.titles = ImmutableList.copyOf(titles);
        this.tooltips = ImmutableList.copyOf(tooltips);
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Room";
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
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        IntegerRange range = ranges.get(index);
        int val = room.getCoordinate(index);
        String title = titles.get(index);
        List<String> tooltip = tooltips.get(index);

        if (range != null)
        {
            TableCellInteger cell = new TableCellInteger(null, val, range.min, range.max);
            cell.addPropertyConsumer(createConsumer(index));
            return new TitledCell(title, cell).withTitleTooltip(tooltip);
        }
        else
        {
            TableCellStringInt cell = new TableCellStringInt(null, val);
            cell.addPropertyConsumer(createConsumer(index));
            return new TitledCell(title, cell).withTitleTooltip(tooltip);
        }
    }

    @Nonnull
    protected Consumer<Integer> createConsumer(int idx)
    {
        return val -> consumer.accept(room = setInDimension(room, idx, val));
    }

    private MazeRoom setInDimension(MazeRoom room, int dimension, int value)
    {
        return this.room.addInDimension(dimension, value - room.getCoordinate(dimension));
    }
}
