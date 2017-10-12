/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import com.google.common.collect.ImmutableList;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by lukas on 13.04.16.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceMazeRoom extends TableDataSourceSegmented
{
    public MazeRoom room;
    protected Consumer<MazeRoom> consumer;

    protected List<IntegerRange> bounds;

    protected final List<String> titles;
    protected final List<List<String>> tooltips;

    public TableDataSourceMazeRoom(MazeRoom room, Consumer<MazeRoom> consumer, List<IntegerRange> bounds, List<String> titles, List<List<String>> tooltips)
    {
        if (titles.size() != room.getDimensions())
            throw new IllegalArgumentException(String.format("titles: %d, dimensions: %d", titles.size(), room.getDimensions()));

        this.room = room;
        this.consumer = consumer;
        this.bounds = bounds;
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
        int val = room.getCoordinate(index);
        String title = titles.get(index);
        List<String> tooltip = tooltips.get(index);

        TableCellPropertyDefault<Integer> cell = bounds != null
                ? new TableCellIntSlider(null, val, bounds.get(index).min, bounds.get(index).max)
                : new TableCellIntTextField(null, val, i -> i > 0);
        cell.addListener(createConsumer(index));
        return new TitledCell(title, cell).withTitleTooltip(tooltip);
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
