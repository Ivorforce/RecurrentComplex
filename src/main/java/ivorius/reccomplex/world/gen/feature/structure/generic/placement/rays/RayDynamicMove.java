/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.placement.rays;

import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellBoolean;
import ivorius.reccomplex.gui.table.cell.TableCellEnum;
import ivorius.reccomplex.gui.table.cell.TableElementCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.reccomplex.world.gen.feature.structure.generic.WorldCache;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorLimit;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.StructurePlaceContext;

import java.util.OptionalInt;

/**
 * Created by lukas on 19.09.16.
 */
public class RayDynamicMove extends FactorLimit.Ray
{
    public Type type;
    public boolean up;

    public RayDynamicMove()
    {
        this(null, Type.STRUCTURE_HEIGHT);
    }

    public RayDynamicMove(Float weight, Type type)
    {
        super(weight);
        this.type = type;
    }

    @Override
    public OptionalInt cast(WorldCache cache, StructurePlaceContext context, int y)
    {
        int mod = up ? 1 : -1;

        switch (type)
        {
            case STRUCTURE_HEIGHT:
                return OptionalInt.of(y + context.boundingBoxSize()[1] * mod);
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String displayString()
    {
        return IvTranslations.format("reccomplex.placer.factors.limit.rays.dynmove.title." + (up ? "up" : "down"), IvTranslations.get("reccomplex.placer.factors.limit.rays.dynmove.type." + IvGsonHelper.serializedName(type)));
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceSegmented(rayTableDataSource(navigator, delegate), new TableDataSourceSupplied(() ->
        {
            TableCellEnum<Type> cell = new TableCellEnum<>(null, type, TableCellEnum.options(Type.values(), "reccomplex.placer.factors.limit.rays.dynmove.type.", true));
            cell.addPropertyConsumer(v -> type = v);
            return new TableElementCell(IvTranslations.get("reccomplex.placer.factors.limit.rays.dynmove.type"), cell);
        }, () ->
        {
            TableCellBoolean cell = new TableCellBoolean(null, up, IvTranslations.get("reccomplex.direction.up"), IvTranslations.get("reccomplex.direction.down"));
            cell.addPropertyConsumer(v -> up = v);
            return new TableElementCell(IvTranslations.get("reccomplex.placer.factors.limit.rays.matcher.direction"), cell);
        }));
    }

    public enum Type
    {
        @SerializedName("structure_height")
        STRUCTURE_HEIGHT
    }
}
