/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.placement;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.editstructure.placer.TableDataSourceFactorLimit;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellFloatNullable;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.json.SerializableStringTypeRegistry;
import ivorius.reccomplex.utils.IntegerRanges;
import ivorius.ivtoolkit.util.LineSelection;
import ivorius.reccomplex.utils.scale.Scales;
import ivorius.ivtoolkit.world.WorldCache;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by lukas on 18.09.16.
 */
public class FactorLimit extends GenericPlacer.Factor
{
    private static SerializableStringTypeRegistry<Ray> rayRegistry = new SerializableStringTypeRegistry<>("ray", "type", Ray.class);

    public static final Gson gson = createGson();

    public final List<Ray> rays = new ArrayList<>();

    public FactorLimit()
    {
        this(1f, Collections.emptyList());
    }

    public FactorLimit(float priority, Collection<Ray> rays)
    {
        super(priority);
        this.rays.addAll(rays);
    }

    private static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();
        rayRegistry.constructGson(builder);
        return builder.create();
    }

    public static SerializableStringTypeRegistry<Ray> getRayRegistry()
    {
        return rayRegistry;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceFactorLimit(this, delegate, navigator);
    }

    @Override
    public List<Pair<LineSelection, Float>> consider(WorldCache cache, LineSelection considerable, @Nullable IvBlockCollection blockCollection, Set<BlockPos> surface, StructurePlaceContext context)
    {
        List<Pair<LineSelection, Float>> consideration = new ArrayList<>();
        int height = cache.world.getHeight();

        int pos = height - 1;
        for (Ray ray : rays)
        {
            int before = pos;
            OptionalInt cast = ray.cast(cache, context, blockCollection, surface, pos);
            if (cast.isPresent())
            {
                pos = cast.getAsInt();

                LineSelection selection = LineSelection.fromRange(IntegerRanges.from(pos, before), true);
                selection.set(considerable, false, false);
                if (ray.weight != null && !selection.isUniform())
                    consideration.add(Pair.of(selection, weight(ray.weight)));
            }
            else
                break;
        }

        return consideration;
    }

    public static class Serializer implements JsonSerializer<FactorLimit>, JsonDeserializer<FactorLimit>
    {
        @Override
        public FactorLimit deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "factorLimit");

            float priority = JsonUtils.getFloat(jsonObject, "priority", 1);
            List<Ray> rays = gson.fromJson(jsonObject.get("rays"), new TypeToken<List<Ray>>() {}.getType());

            return new FactorLimit(priority, rays);
        }

        @Override
        public JsonElement serialize(FactorLimit src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("priority", src.priority);
            jsonObject.add("rays", gson.toJsonTree(src.rays));

            return jsonObject;
        }
    }

    public static abstract class Ray
    {
        @Nullable
        public Float weight;

        public Ray(Float weight)
        {
            this.weight = weight;
        }

        @NotNull
        public static String directionArrow(Boolean up)
        {
            if (up == null) {
                return "▶";
            }

            return up ? "▲" : "▼";
        }

        public abstract OptionalInt cast(WorldCache cache, StructurePlaceContext context, IvBlockCollection collection, Set<BlockPos> surface, int y);

        @SuppressWarnings("NewExpressionSideOnly")
        @SideOnly(Side.CLIENT)
        public TableDataSource rayTableDataSource(TableNavigator navigator, TableDelegate delegate)
        {
            return new TableDataSourceSupplied(() ->
            {
                TableCellFloatNullable cell = new TableCellFloatNullable("value", weight, 0, 0, 1000, "I", "A");
                cell.setScale(Scales.pow(5));
                cell.addListener(v -> weight = v);
                return new TitledCell(IvTranslations.get("reccomplex.placer.factors.limit.ray.weight"), cell)
                        .withTitleTooltip(IvTranslations.formatLines("reccomplex.placer.factors.limit.ray.weight.tooltip"));
            })
            {
                @Nonnull
                @Override
                public String title()
                {
                    return displayString();
                }
            };
        }

        @SideOnly(Side.CLIENT)
        public abstract TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate);

        public String displayString()
        {
            return IvTranslations.get("reccomplex.placer.factors.limit.rays." + getRayRegistry().iDForType(getClass()));
        }
    }
}
