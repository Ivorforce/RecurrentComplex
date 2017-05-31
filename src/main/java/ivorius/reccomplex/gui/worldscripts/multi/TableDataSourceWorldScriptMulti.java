/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.worldscripts.multi;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.worldscripts.TableDataSourceWorldScript;
import ivorius.reccomplex.world.gen.script.WorldScriptMulti;

/**
 * Created by lukas on 06.09.16.
 */
public class TableDataSourceWorldScriptMulti extends TableDataSourceSegmented
{
    public WorldScriptMulti script;

    public TableDataSourceWorldScriptMulti(WorldScriptMulti script, TableDelegate delegate, TableNavigator navigator)
    {
        this.script = script;
        addManagedSegment(0, new TableDataSourceWorldScript(script));
        addManagedSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.worldscript.multi.condition"), script.environmentExpression, null));
        addManagedSegment(2, new TableDataSourceWorldScriptList(script.scripts, delegate, navigator));
    }
}
