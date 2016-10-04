/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.placer;

import ivorius.reccomplex.gui.table.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorLimit;

/**
 * Created by lukas on 04.04.15.
 */
public class TableDataSourceFactorLimit extends TableDataSourceSegmented
{
    public FactorLimit placer;

    public TableDataSourceFactorLimit(FactorLimit factor, TableDelegate delegate, TableNavigator navigator)
    {
        this.placer = factor;
        
        addManagedSegment(0, new TableDataSourcePlacerFactor(factor, delegate, navigator));
        addManagedSegment(1, new TableDataSourceLimitRayList(factor.rays, delegate, navigator));
    }
}
