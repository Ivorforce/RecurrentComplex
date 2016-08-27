/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.table.*;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Created by lukas on 13.04.16.
 */
public class TableDataSourceBlockPos extends TableDataSourceSegmented
{
    private BlockPos coord;
    private Consumer<BlockPos> consumer;

    private IntegerRange rangeX;
    private IntegerRange rangeY;
    private IntegerRange rangeZ;
    private String titleX;
    private String titleY;
    private String titleZ;

    public TableDataSourceBlockPos(BlockPos coord, Consumer<BlockPos> consumer, IntegerRange rangeX, IntegerRange rangeY, IntegerRange rangeZ, String titleX, String titleY, String titleZ)
    {
        this.coord = coord;
        this.consumer = consumer;
        this.rangeX = rangeX;
        this.rangeY = rangeY;
        this.rangeZ = rangeZ;
        this.titleX = titleX;
        this.titleY = titleY;
        this.titleZ = titleZ;
    }

    public TableDataSourceBlockPos(BlockPos coord, Consumer<BlockPos> consumer, IntegerRange range, String titleFormat)
    {
        this.coord = coord;
        this.consumer = consumer;
        this.rangeX = range;
        this.rangeY = range;
        this.rangeZ = range;
        this.titleX = String.format(titleFormat, "X");
        this.titleY = String.format(titleFormat, "Y");
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
        return 3;
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
            case 1:
                range = rangeY;
                val = coord.getY();
                title = titleY;
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
        return cell1 -> consumer.accept(coord = new BlockPos(
                idx == 0 ? cell.getPropertyValue() : coord.getX(),
                idx == 1 ? cell.getPropertyValue() : coord.getY(),
                idx == 2 ? cell.getPropertyValue() : coord.getZ()
        ));
    }
}
