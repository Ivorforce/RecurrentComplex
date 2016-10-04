/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.placement.rays;

import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.WorldCache;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorLimit;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.StructurePlaceContext;

import java.util.OptionalInt;

/**
 * Created by lukas on 19.09.16.
 */
public class RayDynamicPosition extends FactorLimit.Ray
{
    public Type type;

    public RayDynamicPosition()
    {
        this(null, Type.BEDROCK);
    }

    public RayDynamicPosition(Float weight, Type type)
    {
        super(weight);
        this.type = type;
    }

    @Override
    public OptionalInt cast(WorldCache cache, StructurePlaceContext context, int y)
    {
        switch (type)
        {
            case BEDROCK:
                return OptionalInt.of(0);
            case SEALEVEL:
                return OptionalInt.of(cache.world.getSeaLevel());
            case WORLD_HEIGHT:
                return OptionalInt.of(cache.world.getHeight() - 1);
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String displayString()
    {
        return IvTranslations.get("reccomplex.placer.factors.limit.rays.dynpos.type." + IvGsonHelper.serializedName(type));
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceSegmented(rayTableDataSource(navigator, delegate), new TableDataSourceSupplied(() ->
        {
            TableCellEnum<Type> cell = new TableCellEnum<>(null, type, TableCellEnum.options(Type.values(), "reccomplex.placer.factors.limit.rays.dynpos.type.", true));
            cell.addPropertyConsumer(v -> type = v);
            return new TableElementCell(IvTranslations.get("reccomplex.placer.factors.limit.rays.dynpos.type"), cell);
        }));
    }

    public enum Type
    {
        @SerializedName("bedrock")
        BEDROCK,
        @SerializedName("sealevel")
        SEALEVEL,
        @SerializedName("world_height")
        WORLD_HEIGHT
    }
}
