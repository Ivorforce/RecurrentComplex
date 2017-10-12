/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.placement.rays;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.ivtoolkit.util.IvStreams;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellBoolean;
import ivorius.reccomplex.gui.table.cell.TableCellFloatSlider;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.ivtoolkit.world.WorldCache;
import ivorius.reccomplex.utils.expression.PositionedBlockExpression;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorLimit;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.StructurePlaceContext;
import ivorius.ivtoolkit.blocks.BlockSurfaceArea;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Type;
import java.util.OptionalInt;

import static ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorLimit.getRayRegistry;

/**
 * Created by lukas on 19.09.16.
 */
public class RayMatcher extends FactorLimit.Ray
{
    public final PositionedBlockExpression destMatcher = new PositionedBlockExpression(RecurrentComplex.specialRegistry);
    public boolean up;
    public float requiredRatio;

    public RayMatcher()
    {
        this(null, false, .5f, "");
    }

    public RayMatcher(Float weight, boolean up, float requiredRatio, String destExpression)
    {
        super(weight);
        this.up = up;
        this.requiredRatio = requiredRatio;
        this.destMatcher.setExpression(destExpression);
    }

    protected boolean matches(WorldCache cache, BlockSurfaceArea surfaceArea, int y, float needed)
    {
        int[] chances = new int[]{1};
        for (int i : surfaceArea.areaSize()) chances[0] += i;

        int[] need = new int[]{(int) (chances[0] * needed)};
        chances[0] -= need[0];

        IvStreams.visit(surfaceArea.stream(), pos ->
        {
            if (destMatcher.evaluate(() -> PositionedBlockExpression.Argument.at(cache, pos.blockPos(y))))
                return --need[0] > 0;
            else
                return --chances[0] > 0; // Already lost
        });

        return need[0] <= 0;
    }

    @Override
    public OptionalInt cast(WorldCache cache, StructurePlaceContext context, int y)
    {
        BlockSurfaceArea surfaceArea = BlockSurfaceArea.from(context.boundingBox);

        int height = cache.world.getHeight();
        while (true)
        {
            if (y < 0 || y >= height) // Found none
                return OptionalInt.empty();

            if (matches(cache, surfaceArea, y, requiredRatio))
                break;

            y += up ? 1 : -1;
        }

        return OptionalInt.of(y);
    }

    @Override
    public String displayString()
    {
        return IvTranslations.get("reccomplex.placer.factors.limit.rays." + getRayRegistry().iDForType(getClass()) + (up ? ".title.up" : ".title.down"));
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
                }, () ->
                {
                    TableCellFloatSlider cell = new TableCellFloatSlider(null, requiredRatio, 0, 1);
                    cell.addListener(v -> requiredRatio = v);
                    return new TitledCell(IvTranslations.get("reccomplex.placer.factors.limit.rays.matcher.ratio"), cell)
                            .withTitleTooltip(IvTranslations.getLines("reccomplex.placer.factors.limit.rays.matcher.ratio.tooltip"));
                }));
    }

    public static class Serializer implements JsonSerializer<RayMatcher>, JsonDeserializer<RayMatcher>
    {
        @Override
        public RayMatcher deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "rayMatcher");

            Float weight = JsonUtils.has(jsonObject, "weight") ? JsonUtils.getFloat(jsonObject, "weight") : null;

            boolean up = JsonUtils.getBoolean(jsonObject, "up", true);
            float allowedDivergence = JsonUtils.getFloat(jsonObject, "requiredRatio", 0);
            String destExpression = JsonUtils.getString(jsonObject, "destExpression", "");

            return new RayMatcher(weight, up, allowedDivergence, destExpression);
        }

        @Override
        public JsonElement serialize(RayMatcher src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            if (src.weight != null)
                jsonObject.addProperty("weight", src.weight);

            jsonObject.addProperty("up", src.up);
            jsonObject.addProperty("requiredRatio", src.requiredRatio);
            jsonObject.addProperty("destExpression", src.destMatcher.getExpression());

            return jsonObject;
        }
    }
}
