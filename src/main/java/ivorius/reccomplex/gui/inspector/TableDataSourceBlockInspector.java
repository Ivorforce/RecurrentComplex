/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.inspector;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.TableDataSourceBlockState;
import ivorius.reccomplex.gui.nbt.TableDataSourceNBTTagCompound;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 27.08.16.
 */
public class TableDataSourceBlockInspector extends TableDataSourceSegmented
{
    public BlockPos pos;
    public IBlockState state;
    public NBTTagCompound tileEntityData;

    public TableNavigator navigator;
    public TableDelegate delegate;

    public TableDataSourceBlockInspector(BlockPos pos, IBlockState state, TableNavigator navigator, TableDelegate delegate, NBTTagCompound tileEntityData)
    {
        this.pos = pos;
        this.state = state;
        this.navigator = navigator;
        this.delegate = delegate;
        this.tileEntityData = tileEntityData;

        addManagedSegment(0, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceNBTTagCompound(delegate, navigator, this.tileEntityData))
                .enabled(() -> this.tileEntityData != null)
                .addAction(() -> this.tileEntityData != null ? "Remove" : "Add", null, () ->
                {
                    this.tileEntityData = this.tileEntityData != null ? null : new NBTTagCompound();
                    delegate.reloadData();
                })
                .buildDataSource("Tile Entity"));
        addManagedSegment(1, new TableDataSourceBlockState(state, instate -> this.state = instate, navigator, delegate).setShowExtendedProperties(true));
        addManagedSegment(2, new TableDataSourceBlockPos(pos, blockPos -> this.pos = blockPos, null, null, null,
                IvTranslations.get("reccomplex.inspector.position.x"), IvTranslations.get("reccomplex.inspector.position.y"), IvTranslations.get("reccomplex.inspector.position.z")));
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Inspect Block";
    }
}
