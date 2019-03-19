/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.placement.rays;

import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellEnum;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.ivtoolkit.world.WorldCache;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorLimit;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.StructurePlaceContext;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.OptionalInt;
import java.util.Set;

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
    public OptionalInt cast(WorldCache cache, StructurePlaceContext context, IvBlockCollection collection, Set<BlockPos> surface, int y)
    {
        switch (type)
        {
            case BEDROCK:
                return OptionalInt.of(0);
            case SEALEVEL:
                return OptionalInt.of(cache.world.getSeaLevel());
            case WORLD_HEIGHT:
                return OptionalInt.of(cache.world.getHeight() - 1 - collection.height);
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String displayString()
    {
        return String.format("%s %s",
                directionArrow(null),
                IvTranslations.get("reccomplex.placer.factors.limit.rays.dynpos.type." + IvGsonHelper.serializedName(type))
        );
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceSegmented(rayTableDataSource(navigator, delegate), new TableDataSourceSupplied(() ->
        {
            TableCellEnum<Type> cell = new TableCellEnum<>(null, type, TableCellEnum.options(Type.values(), "reccomplex.placer.factors.limit.rays.dynpos.type.", true));
            cell.addListener(v -> type = v);
            return new TitledCell(IvTranslations.get("reccomplex.placer.factors.limit.rays.dynpos.type"), cell);
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
