/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.inspector;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.TableDataSourceBlockState;
import ivorius.reccomplex.gui.table.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Created by lukas on 27.08.16.
 */
public class TableDataSourceBlockInspector extends TableDataSourceSegmented
{
    public BlockPos pos;
    public IBlockState state;

    public TableNavigator navigator;
    public TableDelegate delegate;

    public TableDataSourceBlockInspector(BlockPos pos, IBlockState state, TableNavigator navigator, TableDelegate delegate)
    {
        this.pos = pos;
        this.state = state;
        this.navigator = navigator;
        this.delegate = delegate;

        addManagedSection(0, new TableDataSourceBlockPos(pos, blockPos -> this.pos = pos, null, null, null,
                IvTranslations.get("reccomplex.inspector.position.x"), IvTranslations.get("reccomplex.inspector.position.y"), IvTranslations.get("reccomplex.inspector.position.z")));
        addManagedSection(1, new TableDataSourceBlockState(state, instate -> this.state = instate, navigator, delegate));
    }
}
