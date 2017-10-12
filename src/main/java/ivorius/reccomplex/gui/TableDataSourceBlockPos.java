/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by lukas on 13.04.16.
 */

@SideOnly(Side.CLIENT)
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

        addSegment(0, () -> {
            TableCellPropertyDefault<Integer> x = createRangeCell(rangeX, this.coord.getX());
            x.setTooltip(IvTranslations.getLines("reccomplex.gui.blockpos.x"));
            x.addListener(i -> {
                this.coord = new BlockPos(i, this.coord.getY(), this.coord.getZ());
                consumer.accept(this.coord);
            });

            TableCellPropertyDefault<Integer> y = createRangeCell(rangeY, this.coord.getY());
            y.setTooltip(IvTranslations.getLines("reccomplex.gui.blockpos.y"));
            y.addListener(i -> {
                this.coord = new BlockPos(this.coord.getX(), i, this.coord.getZ());
                consumer.accept(this.coord);
            });

            TableCellPropertyDefault<Integer> z = createRangeCell(rangeZ, this.coord.getZ());
            z.setTooltip(IvTranslations.getLines("reccomplex.gui.blockpos.z"));
            z.addListener(i -> {
                this.coord = new BlockPos(this.coord.getX(), this.coord.getY(), i);
                consumer.accept(this.coord);
            });

            return new TitledCell(title, new TableCellMulti(x, y, z))
                    .withTitleTooltip(tooltip);
        });
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

    public static TableCellPropertyDefault<Integer> createRangeCell(IntegerRange range, int val)
    {
        if (range != null)
            return new TableCellInteger(null, val, range.min, range.max);
        else
            return new TableCellStringInt(null, val);
    }
}
