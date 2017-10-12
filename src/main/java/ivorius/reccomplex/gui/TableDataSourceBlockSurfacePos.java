/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui;

import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.cell.TableCellMulti;
import ivorius.reccomplex.gui.table.cell.TableCellPropertyDefault;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by lukas on 13.04.16.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceBlockSurfacePos extends TableDataSourceSegmented
{
    private BlockSurfacePos coord;
    private Consumer<BlockSurfacePos> consumer;

    private IntegerRange rangeX;
    private IntegerRange rangeZ;

    private String title;
    private List<String> tooltip;

    public TableDataSourceBlockSurfacePos(BlockSurfacePos coord, Consumer<BlockSurfacePos> consumer, IntegerRange rangeX, IntegerRange rangeZ, String title, List<String> tooltip)
    {
        this.coord = coord;
        this.consumer = consumer;
        this.rangeX = rangeX;
        this.rangeZ = rangeZ;
        this.title = title;
        this.tooltip = tooltip;

        addSegment(0, () -> {
            TableCellPropertyDefault<Integer> x = TableDataSourceBlockPos.createRangeCell(rangeX, this.coord.getX());
            x.setTooltip(IvTranslations.getLines("reccomplex.gui.surfacepos.x"));
            x.addListener(i -> {
                this.coord = new BlockSurfacePos(i, this.coord.getZ());
                consumer.accept(this.coord);
            });

            TableCellPropertyDefault<Integer> z = TableDataSourceBlockPos.createRangeCell(rangeZ, this.coord.getZ());
            z.setTooltip(IvTranslations.getLines("reccomplex.gui.surfacepos.z"));
            z.addListener(i -> {
                this.coord = new BlockSurfacePos(this.coord.getX(), i);
                consumer.accept(this.coord);
            });

            return new TitledCell(this.title, new TableCellMulti(x, z))
                    .withTitleTooltip(tooltip);
        });
    }

    public TableDataSourceBlockSurfacePos(BlockSurfacePos coord, Consumer<BlockSurfacePos> consumer, String title, List<String> tooltip)
    {
        this(coord, consumer, null, null, title, tooltip);
        setTooltip(tooltip);
    }

    public TableDataSourceBlockSurfacePos(BlockSurfacePos coord, Consumer<BlockSurfacePos> consumer)
    {
        this(coord, consumer, IvTranslations.get("reccomplex.gui.surfacepos"), IvTranslations.getLines("reccomplex.gui.surfacepos.tooltip"));
    }

    public List<String> getTooltip()
    {
        return tooltip;
    }

    public void setTooltip(List<String> tooltip)
    {
        this.tooltip = tooltip;
    }
}
