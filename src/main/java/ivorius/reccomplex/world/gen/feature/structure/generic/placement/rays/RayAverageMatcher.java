/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.placement.rays;

import com.google.gson.*;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.ivtoolkit.blocks.IvMutableBlockPos;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellBoolean;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.ivtoolkit.world.WorldCache;
import ivorius.reccomplex.utils.expression.PositionedBlockMatcher;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorLimit;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.StructurePlaceContext;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.lang.reflect.Type;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Predicate;

import static ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorLimit.getRayRegistry;

/**
 * Created by lukas on 19.09.16.
 */
public class RayAverageMatcher extends FactorLimit.Ray
{
    public final PositionedBlockMatcher destMatcher = new PositionedBlockMatcher(RecurrentComplex.specialRegistry);
    public boolean up;

    public RayAverageMatcher()
    {
        this(null, false, "blocks:movement & !is:foliage");
    }

    public RayAverageMatcher(Float weight, boolean up, String destExpression)
    {
        super(weight);
        this.up = up;
        this.destMatcher.setExpression(destExpression);
    }

    public static BlockPos findFirstBlock(BlockPos.MutableBlockPos pos, Predicate<BlockPos> predicate, boolean up, int wHeight)
    {
        for (; pos.getY() >= 0 && pos.getY() < wHeight; IvMutableBlockPos.offset(pos, pos, up ? EnumFacing.UP : EnumFacing.DOWN))
        {
            if (predicate.test(pos))
                break;
        }

        // Return one earlier, since it is expected (legacy? mostly because world height did this)
        return IvMutableBlockPos.offset(pos, pos, !up ? EnumFacing.UP : EnumFacing.DOWN);
    }

    // From StructureVillagePieces
    public static int getAverageGroundLevel(boolean up, int y, StructureBoundingBox boundingBox, Predicate<BlockPos> predicate, int wHeight, double samples, Random random)
    {
        TIntList list = new TIntArrayList(boundingBox.getXSize() * boundingBox.getZSize());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int k = boundingBox.minZ; k <= boundingBox.maxZ; ++k)
        {
            for (int l = boundingBox.minX; l <= boundingBox.maxX; ++l)
            {
//                Added
                if (samples >= 1 || random.nextDouble() < samples)
                {
                    pos.setPos(l, y, k);

//                if (structurebb.isVecInside(pos))
                    {
                        list.add(findFirstBlock(pos, predicate, up, wHeight).getY());
                    }
                }
            }
        }

        if (list.isEmpty())
            return -1;
        else
            return averageIgnoringErrors(list.toArray());
    }

    protected static int averageIgnoringErrors(int... values)
    {
        int average = 0;
        for (int val : values)
            average += val;
        average /= values.length;

        int averageDist = 0;
        for (int val : values)
            averageDist += dist(val, average);
        averageDist /= values.length;

        int newAverage = 0;
        int ignored = 0;
        for (int val : values)
        {
            if (dist(val, average) <= averageDist * 2)
                newAverage += val;
            else
                ignored++;
        }

        return newAverage / (values.length - ignored);
    }

    protected static int dist(int val1, int val2)
    {
        return (val1 > val2) ? val1 - val2 : val2 - val1;
    }

    @Override
    public OptionalInt cast(WorldCache cache, StructurePlaceContext context, int y)
    {
        int floorBlocks = context.boundingBox.getXSize() * context.boundingBox.getZSize();
        // Under a chunk size we can use every block no problem.
        // Afterwards we slowly increase total sample size, i.e. decrease chance.
        double samples = floorBlocks < 16 * 16 ? 1 : Math.pow((16f * 16f) / floorBlocks, 0.7f);

        int averageGroundLevel = getAverageGroundLevel(up, y, context.boundingBox,
                blockPos -> destMatcher.evaluate(() -> PositionedBlockMatcher.Argument.at(cache, blockPos)), cache.world.getHeight(),
                samples, context.random);
        return averageGroundLevel >= 0 ? OptionalInt.of(averageGroundLevel) : OptionalInt.empty();
    }

    @Override
    public String displayString()
    {
        return IvTranslations.get("reccomplex.placer.factors.limit.rays." + getRayRegistry().iDForType(getClass()) + (up ? ".title.up" : ".title.down"));
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceSegmented(rayTableDataSource(navigator, delegate),
                TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.placer.factors.limit.rays.matcher.condition"), destMatcher, null),
                new TableDataSourceSupplied(() ->
                {
                    TableCellBoolean cell = new TableCellBoolean(null, up, IvTranslations.get("reccomplex.direction.up"), IvTranslations.get("reccomplex.direction.down"));
                    cell.addPropertyConsumer(v -> up = v);
                    return new TitledCell(IvTranslations.get("reccomplex.placer.factors.limit.rays.matcher.direction"), cell);
                }));
    }

    public static class Serializer implements JsonSerializer<RayAverageMatcher>, JsonDeserializer<RayAverageMatcher>
    {
        @Override
        public RayAverageMatcher deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "rayAverageMatcher");

            Float weight = JsonUtils.has(jsonObject, "weight") ? JsonUtils.getFloat(jsonObject, "weight") : null;

            boolean up = JsonUtils.getBoolean(jsonObject, "up", true);
            String destExpression = JsonUtils.getString(jsonObject, "destExpression", "");

            return new RayAverageMatcher(weight, up, destExpression);
        }

        @Override
        public JsonElement serialize(RayAverageMatcher src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            if (src.weight != null)
                jsonObject.addProperty("weight", src.weight);

            jsonObject.addProperty("up", src.up);
            jsonObject.addProperty("destExpression", src.destMatcher.getExpression());

            return jsonObject;
        }
    }
}
