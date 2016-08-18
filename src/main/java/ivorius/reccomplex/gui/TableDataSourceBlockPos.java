/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.table.*;

import java.util.function.Consumer;

/**
 * Created by lukas on 13.04.16.
 */
public class TableDataSourceBlockPos extends TableDataSourceSegmented implements TableCellPropertyListener
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
        switch (index)
        {
            case 0:
            {
                TableCellInteger cell = new TableCellInteger("x", coord.getX(), rangeX.min, rangeX.max);
                cell.addPropertyListener(this);
                return new TableElementCell(titleX, cell);
            }
            case 1:
            {
                TableCellInteger cell = new TableCellInteger("y", coord.getY(), rangeY.min, rangeY.max);
                cell.addPropertyListener(this);
                return new TableElementCell(titleY, cell);
            }
            case 2:
            {
                TableCellInteger cell = new TableCellInteger("z", coord.getZ(), rangeZ.min, rangeZ.max);
                cell.addPropertyListener(this);
                return new TableElementCell(titleZ, cell);
            }
        }

        return null;
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if (cell.getID() != null)
        {
            switch (cell.getID())
            {
                case "x":
                {
                    consumer.accept(coord = new BlockPos((int) cell.getPropertyValue(), coord.getY(), coord.getZ()));
                    break;
                }
                case "y":
                {
                    consumer.accept(coord = new BlockPos(coord.getX(), (int) cell.getPropertyValue(), coord.getZ()));
                    break;
                }
                case "z":
                {
                    consumer.accept(coord = new BlockPos(coord.getX(), coord.getY(), (int) cell.getPropertyValue()));
                    break;
                }
            }
        }
    }
}
