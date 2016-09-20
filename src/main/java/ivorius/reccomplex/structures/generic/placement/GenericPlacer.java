/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.placement;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.Placer;
import ivorius.reccomplex.structures.generic.WorldCache;
import ivorius.reccomplex.structures.generic.placement.rays.RayAverageMatcher;
import ivorius.reccomplex.structures.generic.placement.rays.RayDynamicPosition;
import ivorius.reccomplex.structures.generic.placement.rays.RayMatcher;
import ivorius.reccomplex.structures.generic.placement.rays.RayMove;
import ivorius.reccomplex.structures.generic.presets.GenericPlacerPresets;
import ivorius.reccomplex.utils.LineSelection;
import ivorius.reccomplex.utils.StructureBoundingBoxes;
import ivorius.reccomplex.utils.presets.PresettedObject;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 25.05.14.
 */
public class GenericPlacer implements Placer
{
    public final List<Factor> factors = new ArrayList<>();

    public GenericPlacer(List<Factor> factors)
    {
        this.factors.addAll(factors);
    }

    @Nonnull
    public static GenericPlacer surfacePlacer()
    {
        return GenericPlacerPresets.instance().preset("surface");
//        return new GenericPlacer(Arrays.asList(
//                // Keep above caves
//                new FactorLimit(1, Arrays.asList(
//                        new RayDynamicPosition(null, RayDynamicPosition.Type.WORLD_HEIGHT),
//                        new RayMatcher(1f, false, .5f, "!(is.air | is.foliage | is.replaceable) & !is.liquid"),
//                        new RayMove(1f, -5))),
//                // Spawn in air or foliage
//                new FactorMatch(1, "!(reccomplex:generic_space | reccomplex:generic_solid)", "(is.air | is.foliage | is.replaceable) & !is.liquid", 0.5f),
//                // Spawn on top of solids
//                new FactorMatch(1, "reccomplex:generic_solid & #0", "!(is.air | is.foliage | is.replaceable)", 0.5f)
//        ));
    }

    @Nonnull
    public static GenericPlacer underwaterPlacer()
    {
        return GenericPlacerPresets.instance().preset("underwater");
//        return new GenericPlacer(Arrays.asList(
//                // Keep above caves
//                new FactorLimit(1, Arrays.asList(
//                        new RayDynamicPosition(null, RayDynamicPosition.Type.WORLD_HEIGHT),
//                        new RayMatcher(1f, false, .5f, "!(is.air | is.foliage | is.replaceable)"),
//                        new RayMove(1f, -5))),
//                // Spawn in air or foliage
//                new FactorMatch(1, "!(reccomplex:generic_space | reccomplex:generic_solid)", "(is.liquid | is.foliage | is.replaceable) & !is.air", 0.5f),
//                // Spawn on top of solids
//                new FactorMatch(1, "reccomplex:generic_solid & #0", "!(is.air | is.foliage | is.replaceable)", 0.5f)
//        ));
    }

    @Override
    public int selectY(StructurePlaceContext context, @Nullable IvBlockCollection blockCollection)
    {
        WorldServer world = context.environment.world;

        WorldCache cache = new WorldCache(world, StructureBoundingBoxes.wholeHeightBoundingBox(world, context.boundingBox));

        LineSelection considerable = LineSelection.fromRange(new IntegerRange(0, world.getHeight() - context.boundingBox.getYSize()), true);

        List<Pair<LineSelection, Float>> considerations = new ArrayList<>();

        factors.forEach(factor ->
        {
            List<Pair<LineSelection, Float>> consideration = factor.consider(cache, considerable, blockCollection, context);

            // Quick remove null considerations
            consideration.stream().filter(p -> p.getRight() <= 0).forEach(p -> considerable.set(p.getLeft(), false));
            consideration = consideration.stream().filter(p -> p.getRight() > 0).collect(Collectors.toList());

            LineSelection considered = new LineSelection(false);
            consideration.forEach(p -> considered.set(p.getLeft(), true));
            considerable.set(considered, false);

            considerations.addAll(consideration);
        });

        Set<Pair<Integer, Double>> applicable = considerable.streamElements(null, true).
                mapToObj(y -> Pair.of(y, considerations.stream()
                        .mapToDouble(pair -> pair.getLeft().isSectionAdditive(pair.getLeft().sectionForIndex(y)) ? pair.getRight() : 0)
                        .reduce(1f, (left, right) -> left * right)))
                .filter(p -> p.getRight() > 0).collect(Collectors.toSet());

        return applicable.size() > 0 ? WeightedSelector.select(context.random, applicable, Pair::getRight).getLeft() : DONT_GENERATE;
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
            return 1f - priority * (1f - weight);
        }

        public String displayString()
        {
            return IvTranslations.get("reccomplex.placer.factors." + FactorRegistry.INSTANCE.getTypeRegistry().iDForType(getClass()));
        }

        public abstract TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate);

        public abstract List<Pair<LineSelection, Float>> consider(WorldCache cache, LineSelection considerable, @Nullable IvBlockCollection blockCollection, StructurePlaceContext context);
    }

    public static class Serializer implements JsonSerializer<GenericPlacer>, JsonDeserializer<GenericPlacer>
    {
        @Nonnull
        public static void readLegacyPlacer(PresettedObject<GenericPlacer> placer, JsonDeserializationContext context, JsonObject jsonObject)
        {
            // Legacy
            SelectionMode selectionMode = jsonObject.has("selectionMode")
                    ? (SelectionMode) context.deserialize(jsonObject.get("selectionMode"), SelectionMode.class)
                    : SelectionMode.SURFACE;

            int minYShift = JsonUtils.getInt(jsonObject, "minY", 0);
            int maxYShift = JsonUtils.getInt(jsonObject, "maxY", 0);

            RayDynamicPosition.Type dynType = null;
            switch (selectionMode)
            {
                case SURFACE:
                default:
                    placer.setContents(new GenericPlacer(Collections.singletonList(new FactorLimit(1, Arrays.asList(
                            new RayDynamicPosition(null, RayDynamicPosition.Type.WORLD_HEIGHT),
                            new RayAverageMatcher(null, false, "(blocks:movement & !is.foliage) | is.liquid"),
                            new RayMove(null, minYShift),
                            new RayMove(1f, maxYShift - minYShift)
                    )))));
                    break;
                case UNDERWATER:
                    placer.setContents(new GenericPlacer(Collections.singletonList(new FactorLimit(1, Arrays.asList(
                            new RayDynamicPosition(null, RayDynamicPosition.Type.WORLD_HEIGHT),
                            new RayAverageMatcher(null, false, "blocks:movement & !is.foliage"),
                            new RayMove(null, minYShift),
                            new RayMove(1f, maxYShift - minYShift)
                    )))));
                    break;
                case LOWEST_EDGE:
                    placer.setContents(new GenericPlacer(Collections.singletonList(new FactorLimit(1, Arrays.asList(
                            new RayDynamicPosition(null, RayDynamicPosition.Type.WORLD_HEIGHT),
                            new RayMatcher(null, false, .1f, "!(is.air | is.foliage | is.replaceable) & !is.liquid"),
                            new RayMove(null, minYShift),
                            new RayMove(1f, maxYShift - minYShift)
                    )))));
                    break;
                case BEDROCK:
                    dynType = RayDynamicPosition.Type.BEDROCK;
                case SEALEVEL:
                    if (dynType == null) dynType = RayDynamicPosition.Type.SEALEVEL;
                case TOP:
                    if (dynType == null) dynType = RayDynamicPosition.Type.WORLD_HEIGHT;

                    placer.setContents(new GenericPlacer(Collections.singletonList(new FactorLimit(1, Arrays.asList(
                            new RayDynamicPosition(null, dynType),
                            new RayMove(null, minYShift),
                            new RayMove(1f, maxYShift - minYShift)
                    )))));
                    break;
            }
        }

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
            LOWEST_EDGE;
        }
    }
}
