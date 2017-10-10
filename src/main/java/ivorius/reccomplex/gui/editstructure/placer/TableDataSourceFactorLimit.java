/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.placer;

import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorLimit;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 04.04.15.
 */

@SideOnly(Side.CLIENT)
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
