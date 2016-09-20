/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.placement.rays;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.WorldCache;
import ivorius.reccomplex.structures.generic.placement.FactorLimit;
import ivorius.reccomplex.structures.generic.placement.StructurePlaceContext;

import static ivorius.reccomplex.structures.generic.placement.FactorLimit.getRayRegistry;

/**
 * Created by lukas on 19.09.16.
 */
public class RayMove extends FactorLimit.Ray
{
    public int distance;

    public RayMove()
    {
        this(null, 1);
    }

    public RayMove(Float weight, int distance)
    {
        super(weight);
        this.distance = distance;
    }

    @Override
    public int cast(WorldCache cache, StructurePlaceContext context, int y)
    {
        return y + distance;
    }

    @Override
    public String displayString()
    {
        return IvTranslations.format(String.format("reccomplex.placer.factors.limit.rays.%s.title%s", getRayRegistry().iDForType(getClass()), weight != null ? ".mark" : ""), String.valueOf(distance));
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceSegmented(rayTableDataSource(navigator, delegate), new TableDataSourceSupplied(() ->
        {
            TableCellStringInt cell = new TableCellStringInt(null, distance);
            cell.addPropertyConsumer(v -> distance = v);
            return new TableElementCell(IvTranslations.get("reccomplex.placer.factors.limit.rays.move.distance"), cell);
        }));
    }
}
