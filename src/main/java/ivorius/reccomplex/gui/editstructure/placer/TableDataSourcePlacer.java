/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.placer;

import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedObject;
import ivorius.reccomplex.gui.table.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.structures.generic.placement.GenericPlacer;
import ivorius.reccomplex.utils.presets.PresettedObject;

/**
 * Created by lukas on 04.04.15.
 */
public class TableDataSourcePlacer extends TableDataSourceSegmented
{
    public TableDataSourcePlacer(PresettedObject<GenericPlacer> object, TableDelegate delegate, TableNavigator navigator)
    {
        addManagedSection(0, new TableDataSourcePresettedObject<>(object, delegate, navigator));

        addManagedSection(1, new TableDataSourcePlacerFactorList(object.getContents().factors, delegate, navigator)
        {
            @Override
            public boolean canEditList()
            {
                return object.isCustom();
            }
        });
    }
}
