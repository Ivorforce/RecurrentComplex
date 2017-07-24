/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.generation;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import ivorius.ivtoolkit.maze.classic.MazeRoom;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.ivtoolkit.world.chunk.Chunks;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceStaticGeneration;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.world.gen.feature.structure.Placer;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.GenericPlacer;
import ivorius.reccomplex.utils.expression.DimensionExpression;
import ivorius.reccomplex.world.gen.feature.structure.generic.presets.GenericPlacerPresets;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.reccomplex.utils.presets.PresettedObject;
import ivorius.reccomplex.utils.presets.PresettedObjects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by lukas on 21.02.15.
 */
public class StaticGeneration extends GenerationType
{
    private static Gson gson = createGson();

    public final PresettedObject<GenericPlacer> placer = new PresettedObject<>(GenericPlacerPresets.instance(), null);
    public DimensionExpression dimensionExpression;

    public boolean relativeToSpawn;
    public BlockSurfacePos position;

    @Nullable
    public Pattern pattern;

    public StaticGeneration()
    {
        this(null, ExpressionCache.of(new DimensionExpression(), "0"), true, BlockSurfacePos.ORIGIN, null);
    }

    public StaticGeneration(@Nullable String id, DimensionExpression dimensionExpression, boolean relativeToSpawn, BlockSurfacePos position, Pattern pattern)
    {
        super(id != null ? id : randomID(StaticGeneration.class));
        this.dimensionExpression = dimensionExpression;
        this.relativeToSpawn = relativeToSpawn;
        this.position = position;
        this.pattern = pattern;

        this.placer.setPreset("surface");
    }

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(StaticGeneration.class, new StaticGeneration.Serializer());
        builder.registerTypeAdapter(GenericPlacer.class, new GenericPlacer.Serializer());

        return builder.create();
    }

    public static Gson getGson()
    {
        return gson;
    }

    public static Stream<Triple<Structure<?>, StaticGeneration, BlockSurfacePos>> structuresAt(StructureRegistry registry, ChunkPos chunkPos, final World world, final BlockPos spawnPos)
    {
        final Predicate<Pair<Structure<?>, StaticGeneration>> predicate = input ->
        {
            StaticGeneration info = input.getRight();

            return info.dimensionExpression.test(world.provider)
                    && (info.pattern != null || Chunks.contains(chunkPos, info.getPos(spawnPos)));
        };
        Stream<Pair<Structure<?>, StaticGeneration>> statics = registry.getGenerationTypes(StaticGeneration.class).stream().filter(predicate);

        return statics.flatMap(pair ->
        {
            StaticGeneration info = pair.getRight();
            //noinspection ConstantConditions
            return info.hasPattern()
                    ? Chunks.repeatIntersections(chunkPos, info.getPos(spawnPos), info.pattern.repeatX, info.pattern.repeatZ).map(pos -> Triple.of(pair.getLeft(), info, pos))
                    : Stream.of(Triple.of(pair.getLeft(), info, info.getPos(spawnPos)));
        });
    }

    public BlockSurfacePos getPosition()
    {
        return position;
    }

    public void setPosition(BlockSurfacePos position)
    {
        this.position = position;
    }

    @Nonnull
    @Override
    public String id()
    {
        return id;
    }

    @Override
    public void setID(@Nonnull String id)
    {
        this.id = id;
    }

    @Override
    public String displayString()
    {
        if (hasPattern())
            return IvTranslations.format("reccomplex.generationInfo.static.summary.pattern", String.valueOf(pattern.repeatX), String.valueOf(pattern.repeatZ));
        else if (relativeToSpawn)
            return IvTranslations.format("reccomplex.generationInfo.static.summary.spawn", String.valueOf(position.x), String.valueOf(position.z));
        else
            return IvTranslations.format("reccomplex.generationInfo.static.summary.nospawn", String.valueOf(position.x), String.valueOf(position.z));
    }

    @Nullable
    @Override
    public Placer placer()
    {
        return placer.getContents();
    }

    @Override
    public TableDataSource tableDataSource(Function<MazeRoom, BlockPos> realWorldMapper, TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceStaticGeneration(navigator, delegate, this);
    }

    public BlockSurfacePos getPos(BlockPos spawnPos)
    {
        return new BlockSurfacePos(relativeToSpawn ? spawnPos.getX() + position.x : position.x, relativeToSpawn ? spawnPos.getZ() + position.z : position.z);
    }

    public boolean hasPattern()
    {
        return pattern != null;
    }

    public static class Pattern
    {
        @SerializedName("repeatX")
        public int repeatX = 16;
        @SerializedName("repeatZ")
        public int repeatZ = 16;

        @SerializedName("randomShiftX")
        public int randomShiftX = 0;
        @SerializedName("randomShiftZ")
        public int randomShiftZ = 0;
    }

    public static class Serializer implements JsonSerializer<StaticGeneration>, JsonDeserializer<StaticGeneration>
    {
        @Override
        public StaticGeneration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "vanillaStructureSpawnInfo");

            String id = readID(jsonObject);

            String dimension = JsonUtils.getString(jsonObject, "dimensions", "");

            boolean relativeToSpawn = JsonUtils.getBoolean(jsonObject, "relativeToSpawn", false);
            int positionX = JsonUtils.getInt(jsonObject, "positionX", 0);
            int positionZ = JsonUtils.getInt(jsonObject, "positionZ", 0);

            Pattern pattern = jsonObject.has("pattern") ? gson.fromJson(jsonObject.get("pattern"), Pattern.class) : null;

            StaticGeneration staticGenInfo = new StaticGeneration(id, ExpressionCache.of(new DimensionExpression(), dimension), relativeToSpawn, new BlockSurfacePos(positionX, positionZ), pattern);

            if (!PresettedObjects.read(jsonObject, gson, staticGenInfo.placer, "placerPreset", "placer", new TypeToken<GenericPlacer>(){}.getType())
                    && jsonObject.has("generationY"))
            {
                // Legacy
                GenericPlacer.Serializer.readLegacyPlacer(staticGenInfo.placer, context, JsonUtils.getJsonObject(jsonObject, "generationY", new JsonObject()));
            }

            return staticGenInfo;
        }

        @Override
        public JsonElement serialize(StaticGeneration src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            PresettedObjects.write(jsonObject, gson, src.placer, "placerPreset", "placer");
            jsonObject.addProperty("dimensions", src.dimensionExpression.getExpression());

            jsonObject.addProperty("relativeToSpawn", src.relativeToSpawn);
            jsonObject.addProperty("positionX", src.position.x);
            jsonObject.addProperty("positionZ", src.position.z);

            if (src.pattern != null)
                jsonObject.add("pattern", gson.toJsonTree(src.pattern));

            return jsonObject;
        }
    }
}
