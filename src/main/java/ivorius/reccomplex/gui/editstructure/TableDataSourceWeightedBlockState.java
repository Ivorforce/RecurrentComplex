/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.TableDataSourceBlockState;
import ivorius.reccomplex.gui.nbt.TableDataSourceNBTTagCompound;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.WeightedBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by lukas on 05.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceWeightedBlockState extends TableDataSourceSegmented
{
    private WeightedBlockState weightedBlockState;

    public TableDataSourceWeightedBlockState(WeightedBlockState weightedBlockState, TableNavigator navigator, TableDelegate delegate)
    {
        this.weightedBlockState = weightedBlockState;

        addSegment(0, () -> {
            return RCGuiTables.defaultWeightElement(val -> weightedBlockState.weight = TableCells.toDouble(val), weightedBlockState.weight);
        });
        addSegment(1, new TableDataSourceBlockState(weightedBlockState.state, state -> weightedBlockState.state = state, navigator, delegate, "Block", "Metadata"));
        addSegment(2, tileEntitySegment(navigator, delegate, () -> weightedBlockState.tileEntityInfo, val -> weightedBlockState.tileEntityInfo = val));
    }

    @Nonnull
    public static TableDataSource tileEntitySegment(final TableNavigator navigator, final TableDelegate delegate, Supplier<NBTTagCompound> supplier, Consumer<NBTTagCompound> consumer)
    {
        return TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceNBTTagCompound(delegate, navigator, supplier.get())
                {
                    @Nonnull
                    @Override
                    public String title()
                    {
                        return "Tile Entity";
                    }
                })
                .enabled(() -> supplier.get() != null)
                .addAction(() ->{
                    consumer.accept(supplier.get() != null ? null : new NBTTagCompound());
                    delegate.reloadData();
                }, () -> supplier.get() != null ? "Remove" : "Add", null).withTitle("Tile Entity").buildDataSource();
    }

    @Nonnull
    @Override
    public String title()
    {
        return weightedBlockState.state.getBlock().getLocalizedName();
    }
}
