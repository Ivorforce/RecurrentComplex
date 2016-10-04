/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.reccomplex.gui.table.cell.TableCellInteger;
import ivorius.reccomplex.gui.table.cell.TableCellStringInt;
import ivorius.reccomplex.gui.table.cell.TableElementCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
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
        return property -> consumer.accept(coord = new BlockPos(
                idx == 0 ? property : coord.getX(),
                idx == 1 ? property : coord.getY(),
                idx == 2 ? property : coord.getZ()
        ));
    }
}
