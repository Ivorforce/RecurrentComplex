/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.placer;

import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedObject;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.GenericPlacer;
import ivorius.reccomplex.utils.presets.PresettedObject;

/**
 * Created by lukas on 04.04.15.
 */
public class TableDataSourcePlacer extends TableDataSourceSegmented
{
    public TableDataSourcePlacer(PresettedObject<GenericPlacer> object, TableDelegate delegate, TableNavigator navigator)
    {
        addManagedSegment(0, new TableDataSourcePresettedObject<>(object, delegate, navigator)
                .withApplyPresetAction(() -> addPresetSegments(object, delegate, navigator)));

        addPresetSegments(object, delegate, navigator);
    }

    public void addPresetSegments(final PresettedObject<GenericPlacer> object, final TableDelegate delegate, final TableNavigator navigator)
    {
        addManagedSegment(1, new TableDataSourcePlacerFactorList(object.getContents().factors, delegate, navigator)
        {
            @Override
            public boolean canEditList()
            {
                return object.isCustom();
            }
        });
    }
}
