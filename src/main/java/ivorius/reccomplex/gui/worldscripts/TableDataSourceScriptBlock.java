/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.worldscripts;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.block.TileEntityScriptBlock;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.worldscripts.multi.TableDataSourceWorldScriptList;
import ivorius.reccomplex.gui.worldscripts.multi.TableDataSourceWorldScriptMulti;
import ivorius.reccomplex.world.gen.script.WorldScriptMulti;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 06.09.16.
 */
public class TableDataSourceScriptBlock extends TableDataSourceSegmented
{
    public TileEntityScriptBlock script;

    public TableDataSourceScriptBlock(TileEntityScriptBlock script, TableDelegate delegate, TableNavigator navigator)
    {
        this.script = script;
        addManagedSegment(0, new TableDataSourceWorldScriptMulti(script.script, delegate, navigator));
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Script Block";
    }
}
