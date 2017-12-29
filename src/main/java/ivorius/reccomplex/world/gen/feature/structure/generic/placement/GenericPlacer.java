/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.placement;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.world.gen.feature.structure.Placer;
import ivorius.ivtoolkit.world.WorldCache;
import ivorius.ivtoolkit.util.LineSelection;
import ivorius.ivtoolkit.util.LineSelections;
import ivorius.ivtoolkit.world.chunk.gen.StructureBoundingBoxes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.05.14.
 */
public class GenericPlacer
{
    public final List<Factor> factors = new ArrayList<>();

    public GenericPlacer()
    {
    }

    public GenericPlacer(List<Factor> factors)
    {
        this.factors.addAll(factors);
    }

    public int place(StructurePlaceContext context, @Nullable IvBlockCollection blockCollection, Set<BlockPos> surface)
    {
        if (factors.isEmpty())
            return Placer.DONT_GENERATE;

        WorldServer world = context.environment.world;

        WorldCache cache = new WorldCache(world, StructureBoundingBoxes.wholeHeightBoundingBox(world, context.boundingBox));

        LineSelection considerable = LineSelection.fromRange(new IntegerRange(0, world.getHeight() - context.boundingBox.getYSize()), true);

        List<Pair<LineSelection, Float>> considerations = new ArrayList<>();

        factors.forEach(factor ->
        {
            List<Pair<LineSelection, Float>> consideration = factor.consider(cache, considerable, blockCollection, surface, context);

            // Quick remove null considerations
            consideration.stream().filter(p -> p.getRight() <= 0).forEach(p -> considerable.set(p.getLeft(), true, false));
            consideration = consideration.stream().filter(p -> p.getRight() > 0).collect(Collectors.toList());

            // Remove everything not even considered
            considerable.set(LineSelections.combine(consideration.stream().map(Pair::getLeft), true), false, false);

            considerations.addAll(consideration);
        });

        Set<Pair<Integer, Double>> applicable = considerable.streamElements(null, true).
                mapToObj(y -> Pair.of(y, considerations.stream()
                        .mapToDouble(pair -> pair.getLeft().isSectionAdditive(pair.getLeft().sectionForIndex(y)) ? pair.getRight() : 1)
                        .reduce(1f, (left, right) -> left * right)))
                .filter(p -> p.getRight() > 0).collect(Collectors.toSet());

        return applicable.size() > 0 ? WeightedSelector.select(context.random, applicable, Pair::getRight).getLeft() : Placer.DONT_GENERATE;
    }

    public static abstract class Factor
    {
        public float priority;

        public Factor(float priority)
        {
            this.priority = priority;
        }

        public float weight(float weight)
        {
            return (float) Math.pow(weight, priority);
        }

        public String displayString()
        {
            return IvTranslations.get("reccomplex.placer.factors." + FactorRegistry.INSTANCE.getTypeRegistry().iDForType(getClass()));
        }

        @SideOnly(Side.CLIENT)
        public abstract TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate);

        public abstract List<Pair<LineSelection, Float>> consider(WorldCache cache, LineSelection considerable, @Nullable IvBlockCollection blockCollection, Set<BlockPos> surface, StructurePlaceContext context);
    }

    public static class Serializer implements JsonSerializer<GenericPlacer>, JsonDeserializer<GenericPlacer>
    {
        @Override
        public GenericPlacer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "placer");

            List<Factor> factors = FactorRegistry.INSTANCE.gson.fromJson(jsonObject.get("factors"), new TypeToken<List<Factor>>(){}.getType());
            if (factors == null) factors = Collections.emptyList();

            return new GenericPlacer(factors);

        }

        @Override
        public JsonElement serialize(GenericPlacer src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("factors", FactorRegistry.INSTANCE.gson.toJsonTree(src.factors));

            return jsonObject;
        }

        // Legacy
        public enum SelectionMode
        {
            @SerializedName("bedrock")
            BEDROCK,
            @SerializedName("surface")
            SURFACE,
            @SerializedName("sealevel")
            SEALEVEL,
            @SerializedName("underwater")
            UNDERWATER,
            @SerializedName("top")
            TOP,
            @SerializedName("lowestedge")
            LOWEST_EDGE,
        }
    }
}
