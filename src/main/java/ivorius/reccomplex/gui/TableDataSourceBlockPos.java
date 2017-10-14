/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.cell.TableCellMulti;
import ivorius.reccomplex.gui.table.cell.TableCellPropertyDefault;
import ivorius.reccomplex.gui.table.cell.TableCellIntTextField;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Created by lukas on 13.04.16.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceBlockPos extends TableDataSourceSegmented
{
    private BlockPos coord;
    private Consumer<BlockPos> consumer;

    private String title;
    private List<String> tooltip;

    protected BooleanSupplier enabled = () -> true;

    public TableDataSourceBlockPos(BlockPos coord, Consumer<BlockPos> consumer, String title)
    {
        this.coord = coord;
        this.consumer = consumer;
        this.title = title;

        addSegment(0, () -> {
            boolean enabled = this.enabled.getAsBoolean();

            TableCellPropertyDefault<Integer> x = new TableCellIntTextField(null, this.coord.getX());
            x.setTooltip(IvTranslations.getLines("reccomplex.gui.blockpos.x"));
            x.addListener(i -> {
                this.coord = new BlockPos(i, this.coord.getY(), this.coord.getZ());
                consumer.accept(this.coord);
            });
            x.setEnabled(enabled);

            TableCellPropertyDefault<Integer> y = new TableCellIntTextField(null, this.coord.getY());
            y.setTooltip(IvTranslations.getLines("reccomplex.gui.blockpos.y"));
            y.addListener(i -> {
                this.coord = new BlockPos(this.coord.getX(), i, this.coord.getZ());
                consumer.accept(this.coord);
            });
            y.setEnabled(enabled);

            TableCellPropertyDefault<Integer> z = new TableCellIntTextField(null, this.coord.getZ());
            z.setTooltip(IvTranslations.getLines("reccomplex.gui.blockpos.z"));
            z.addListener(i -> {
                this.coord = new BlockPos(this.coord.getX(), this.coord.getY(), i);
                consumer.accept(this.coord);
            });
            z.setEnabled(enabled);

            return new TitledCell(title, new TableCellMulti(x, y, z))
                    .withTitleTooltip(tooltip);
        });
    }

    public TableDataSourceBlockPos(BlockPos coord, Consumer<BlockPos> consumer, String title, List<String> tooltip)
    {
        this(coord, consumer, title);
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

    public void setEnabled(BooleanSupplier enabled)
    {
        this.enabled = enabled;
    }
}
