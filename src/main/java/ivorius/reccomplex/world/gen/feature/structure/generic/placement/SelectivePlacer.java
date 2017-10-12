/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.placement;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.presets.PresettedObject;
import ivorius.reccomplex.utils.presets.PresettedObjects;
import ivorius.reccomplex.world.gen.feature.structure.Placer;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.rays.RayAverageMatcher;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.rays.RayDynamicPosition;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.rays.RayMatcher;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.rays.RayMove;
import ivorius.reccomplex.world.gen.feature.structure.generic.presets.GenericPlacerPresets;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;

public class SelectivePlacer implements Placer
{
    public static Gson gson = createGson();

    public final PresettedObject<GenericPlacer> placer = new PresettedObject<>(GenericPlacerPresets.instance(), null);

    public int baseline;

    public SelectivePlacer()
    {
        placer.setPreset("surface");
    }

    public SelectivePlacer(GenericPlacer placer, int baseline)
    {
        this.placer.setContents(placer);
        this.baseline = baseline;
    }

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(SelectivePlacer.class, new SelectivePlacer.Serializer());
        builder.registerTypeAdapter(GenericPlacer.class, new GenericPlacer.Serializer());

        return builder.create();
    }

    @Nonnull
    public static SelectivePlacer surfacePlacer(int baseline)
    {
        SelectivePlacer surface = new SelectivePlacer();
        surface.placer.setPreset("surface");
        surface.baseline = baseline;
        return surface;
    }

    @Override
    public int place(StructurePlaceContext context, IvBlockCollection blockCollection)
    {
        return placer.getContents().place(context, blockCollection, baseline);
    }

    public static class Serializer implements JsonSerializer<SelectivePlacer>, JsonDeserializer<SelectivePlacer>
    {
        @Nonnull
        public static SelectivePlacer readLegacyPlacer(JsonDeserializationContext context, JsonObject jsonObject)
        {
            SelectivePlacer placer;

            // Legacy
            GenericPlacer.Serializer.SelectionMode selectionMode = jsonObject.has("selectionMode")
                    ? (GenericPlacer.Serializer.SelectionMode) context.deserialize(jsonObject.get("selectionMode"), GenericPlacer.Serializer.SelectionMode.class)
                    : GenericPlacer.Serializer.SelectionMode.SURFACE;

            int minYShift = JsonUtils.getInt(jsonObject, "minY", 0);
            int maxYShift = JsonUtils.getInt(jsonObject, "maxY", 0);

            RayDynamicPosition.Type dynType = null;
            switch (selectionMode)
            {
                case SURFACE:
                default:
                    // No blurry baselines anymore
                    placer = SelectivePlacer.surfacePlacer(-(minYShift + maxYShift) / 2);
                    break;
                case UNDERWATER:
                    placer = new SelectivePlacer(new GenericPlacer(Collections.singletonList(new FactorLimit(1, Arrays.asList(
                            new RayDynamicPosition(null, RayDynamicPosition.Type.WORLD_HEIGHT),
                            new RayAverageMatcher(null, false, "blocks:movement & !is:foliage"),
                            new RayMove(null, minYShift),
                            new RayMove(1f, maxYShift - minYShift)
                    )))), 0);
                    break;
                case LOWEST_EDGE:
                    placer = new SelectivePlacer(new GenericPlacer(Collections.singletonList(new FactorLimit(1, Arrays.asList(
                            new RayDynamicPosition(null, RayDynamicPosition.Type.WORLD_HEIGHT),
                            new RayMatcher(null, false, .9f, "!(is:air | is:foliage | is:replaceable) & !is:liquid"),
                            new RayMove(null, minYShift),
                            new RayMove(1f, maxYShift - minYShift)
                    )))), 0);
                    break;
                case BEDROCK:
                    dynType = RayDynamicPosition.Type.BEDROCK;
                case SEALEVEL:
                    if (dynType == null) dynType = RayDynamicPosition.Type.SEALEVEL;
                case TOP:
                    if (dynType == null) dynType = RayDynamicPosition.Type.WORLD_HEIGHT;

                    placer = new SelectivePlacer(new GenericPlacer(Collections.singletonList(new FactorLimit(1, Arrays.asList(
                            new RayDynamicPosition(null, dynType),
                            new RayMove(null, minYShift),
                            new RayMove(1f, maxYShift - minYShift)
                    )))), 0);
                    break;
            }

            return placer;
        }

        @Override
        public SelectivePlacer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            SelectivePlacer selectivePlacer = new SelectivePlacer();

            JsonObject jsonObject = json.getAsJsonObject();

            if (!PresettedObjects.read(jsonObject, gson, selectivePlacer.placer, "placerPreset", "placer", new TypeToken<GenericPlacer>() {}.getType())
                    && jsonObject.has("generationY"))
            {
                // Legacy
                return readLegacyPlacer(context, JsonUtils.getJsonObject(jsonObject, "generationY", new JsonObject()));
            }

            selectivePlacer.baseline = JsonUtils.getInt(jsonObject, "baseline", 0);

            return selectivePlacer;
        }

        @Override
        public JsonElement serialize(SelectivePlacer src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            PresettedObjects.write(jsonObject, gson, src.placer, "placerPreset", "placer");
            jsonObject.addProperty("baseline", src.baseline);

            return jsonObject;
        }
    }
}
