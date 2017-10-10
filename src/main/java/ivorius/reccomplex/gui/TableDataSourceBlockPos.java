/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import net.minecraft.util.math.BlockPos;

import java.util.List;
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

    private String title;
    private List<String> tooltip;

    public TableDataSourceBlockPos(BlockPos coord, Consumer<BlockPos> consumer, IntegerRange rangeX, IntegerRange rangeY, IntegerRange rangeZ, String title)
    {
        this.coord = coord;
        this.consumer = consumer;
        this.rangeX = rangeX;
        this.rangeY = rangeY;
        this.rangeZ = rangeZ;
        this.title = title;
    }

    public TableDataSourceBlockPos(BlockPos coord, Consumer<BlockPos> consumer, String title, List<String> tooltip)
    {
        this(coord, consumer, null, null, null, title);
        setTooltip(tooltip);
    }

    public TableDataSourceBlockPos(BlockPos coord, Consumer<BlockPos> consumer)
    {
        this(coord, consumer, IvTranslations.get("reccomplex.gui.blockpos"), IvTranslations.getLines("reccomplex.gui.blockpos.tooltip"));
    }

    public List<String> getTooltip()
    {
        return tooltip;
    }

    public void setTooltip(List<String> tooltip)
    {
        this.tooltip = tooltip;
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return 1;
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        TableCellPropertyDefault<Integer> x = create(rangeX, coord.getX());
        x.setTooltip(IvTranslations.getLines("reccomplex.gui.blockpos.x"));
        x.addListener(i -> {
            coord = new BlockPos(i, coord.getY(), coord.getZ());
            consumer.accept(coord);
        });

        TableCellPropertyDefault<Integer> y = create(rangeY, coord.getY());
        y.setTooltip(IvTranslations.getLines("reccomplex.gui.blockpos.y"));
        y.addListener(i -> {
            coord = new BlockPos(coord.getX(), i, coord.getZ());
            consumer.accept(coord);
        });

        TableCellPropertyDefault<Integer> z = create(rangeZ, coord.getZ());
        z.setTooltip(IvTranslations.getLines("reccomplex.gui.blockpos.z"));
        z.addListener(i -> {
            coord = new BlockPos(coord.getX(), coord.getY(), i);
            consumer.accept(coord);
        });

        return new TitledCell(title, new TableCellMulti(x, y, z))
                .withTitleTooltip(tooltip);
    }

    protected TableCellPropertyDefault<Integer> create(IntegerRange range, int val)
    {
        if (range != null)
            return new TableCellInteger(null, val, range.min, range.max);
        else
            return new TableCellStringInt(null, val);
    }
}
