/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.placement.rays;

import com.google.gson.*;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.blocks.IvMutableBlockPos;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.ivtoolkit.world.WorldCache;
import ivorius.ivtoolkit.world.chunk.gen.StructureBoundingBoxes;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellBoolean;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.expression.PositionedBlockExpression;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorLimit;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.StructurePlaceContext;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Type;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorLimit.getRayRegistry;

/**
 * Created by lukas on 19.09.16.
 */
public class RayAverageMatcher extends FactorLimit.Ray
{
    public final PositionedBlockExpression destMatcher = new PositionedBlockExpression(RecurrentComplex.specialRegistry);
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
                return pos;
        }

        return null;
    }

    // From StructureVillagePieces
    public static int getAverageGroundLevel(boolean up, int y, Set<BlockPos> surface, Predicate<BlockPos> predicate, int wHeight, double samples, Random random)
    {
        TIntList list = new TIntArrayList(surface.size());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (BlockPos surfacePos : surface)
        {
//                Added
            if (samples >= 1 || random.nextDouble() < samples)
            {
                pos.setPos(surfacePos.getX(), surfacePos.getY() + y, surfacePos.getZ());

//                if (structurebb.isVecInside(pos))
                {
                    // Ignore voiding rays
                    BlockPos found = findFirstBlock(pos, predicate, up, wHeight);
                    if (found != null)
                        list.add(found.getY() - surfacePos.getY());
                }
            }
        }

        if (list.isEmpty())
            return -1;
        else
            return robustAverage(list.toArray());
    }

    protected static int robustAverage(int... values)
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

    public static Set<BlockPos> shifted(StructurePlaceContext context, IvBlockCollection collection, Set<BlockPos> surface)
    {
        int[] size = collection.area().areaSize();
        BlockPos lower = StructureBoundingBoxes.min(context.boundingBox);

        return surface.stream()
                .map(p -> context.transform.apply(p, size).add(lower))
                .collect(Collectors.toSet());
    }

    @Override
    public OptionalInt cast(WorldCache cache, StructurePlaceContext context, IvBlockCollection collection, Set<BlockPos> surface, int y)
    {
        int floorBlocks = surface.size();
        // Under a chunk size we can use every block no problem.
        // Afterwards we slowly increase total sample size, i.e. decrease chance.
        double samples = floorBlocks < 16 * 16 ? 1 : Math.pow((16f * 16f) / floorBlocks, 0.7f);

        Set<BlockPos> shiftedSurface = shifted(context, collection, surface);

        int averageGroundLevel = getAverageGroundLevel(up, y, shiftedSurface,
                blockPos -> destMatcher.evaluate(() -> PositionedBlockExpression.Argument.at(cache, blockPos)), cache.world.getHeight(),
                samples, context.random);
        return averageGroundLevel >= 0 ? OptionalInt.of(averageGroundLevel) : OptionalInt.empty();
    }

    @Override
    public String displayString()
    {
        return String.format("%s %s",
                directionArrow(up),
                IvTranslations.get("reccomplex.placer.factors.limit.rays.average")
        );
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceSegmented(rayTableDataSource(navigator, delegate),
                TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.placer.factors.limit.rays.matcher.condition"), destMatcher, null),
                new TableDataSourceSupplied(() ->
                {
                    TableCellBoolean cell = new TableCellBoolean(null, up, IvTranslations.get("reccomplex.direction.up"), IvTranslations.get("reccomplex.direction.down"));
                    cell.addListener(v -> up = v);
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
