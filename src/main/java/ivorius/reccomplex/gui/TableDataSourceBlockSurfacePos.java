/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.utils.BlockSurfacePos;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Created by lukas on 13.04.16.
 */
public class TableDataSourceBlockSurfacePos extends TableDataSourceSegmented
{
    private BlockSurfacePos coord;
    private Consumer<BlockSurfacePos> consumer;

    private IntegerRange rangeX;
    private IntegerRange rangeZ;
    private String titleX;
    private String titleZ;

    public TableDataSourceBlockSurfacePos(BlockSurfacePos coord, Consumer<BlockSurfacePos> consumer, IntegerRange rangeX, IntegerRange rangeZ, String titleX, String titleZ)
    {
        this.coord = coord;
        this.consumer = consumer;
        this.rangeX = rangeX;
        this.rangeZ = rangeZ;
        this.titleX = titleX;
        this.titleZ = titleZ;
    }

    public TableDataSourceBlockSurfacePos(BlockSurfacePos coord, Consumer<BlockSurfacePos> consumer, IntegerRange range, String titleFormat)
    {
        this.coord = coord;
        this.consumer = consumer;
        this.rangeX = range;
        this.rangeZ = range;
        this.titleX = String.format(titleFormat, "X");
        this.titleZ = String.format(titleFormat, "Z");
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return 2;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        IntegerRange range;
        int val;
        String title;

        switch (index)
        {
            case 0:
                range = rangeX;
                val = coord.getX();
                title = titleX;
                break;
            default:
                range = rangeZ;
                val = coord.getZ();
                title = titleZ;
                break;
        }

        if (range != null)
        {
            TableCellInteger cell = new TableCellInteger(null, val, range.min, range.max);
            cell.addPropertyConsumer(createConsumer(index));
            return new TableElementCell(title, cell);
        }
        else
        {
            TableCellStringInt cell = new TableCellStringInt(null, val);
            cell.addPropertyConsumer(createConsumer(index));
            return new TableElementCell(title, cell);
        }
    }

    @Nonnull
    protected Consumer<Integer> createConsumer(int idx)
    {
        return val -> consumer.accept(coord = new BlockSurfacePos(
                idx == 0 ? val : coord.getX(),
                idx == 1 ? val : coord.getZ()
        ));
    }
}
