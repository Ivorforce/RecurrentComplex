/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.generation;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.ivtoolkit.world.chunk.Chunks;
import ivorius.reccomplex.client.rendering.MazeVisualizationContext;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceStaticGeneration;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.DimensionExpression;
import ivorius.reccomplex.world.gen.feature.structure.Placer;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.GenericPlacer;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.SelectivePlacer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by lukas on 21.02.15.
 */
public class StaticGeneration extends GenerationType
{
    private static Gson gson = createGson();

    public SelectivePlacer placer;
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

        this.placer = new SelectivePlacer();
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
            Stream<BlockSurfacePos> stream;
            //noinspection ConstantConditions
            if (info.hasPattern()) {
                stream = Chunks
                        .repeatIntersections(chunkPos, info.getPos(spawnPos), info.pattern.repeatX, info.pattern.repeatZ)
                        .map(pos ->
                                new BlockSurfacePos(
                                        pos.getX() + (int) (((Math.random() - 0.5f) * 2) * info.pattern.randomShiftX),
                                        pos.getZ() + (int) (((Math.random() - 0.5f) * 2) * info.pattern.randomShiftZ)))
                        .filter(pos -> {
                            String biome = world.getBiome(pos.blockPos(0)).getRegistryName().toString();
                            return info.pattern.biomeList(biome);
                        });
            } else {
                stream = Stream.of(info.getPos(spawnPos));
            }
            return stream.map(pos -> Triple.of(pair.getLeft(), info, pos));
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
        return placer;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TableDataSource tableDataSource(MazeVisualizationContext mazeVisualizationContext, TableNavigator navigator, TableDelegate delegate)
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

        @SerializedName("biomeList")
        public String biomeList = "";
        @SerializedName("whitelist")
        public boolean white = false;

        public boolean biomeList(String biome) {
            List<String> biomes = Arrays.asList(biomeList.replace(" ", "").split(","));
            return white == biomes.contains(biome);
        }
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

            staticGenInfo.placer = SelectivePlacer.gson.fromJson(json, SelectivePlacer.class);

            return staticGenInfo;
        }

        @Override
        public JsonElement serialize(StaticGeneration src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = (JsonObject) SelectivePlacer.gson.toJsonTree(src.placer);

            jsonObject.addProperty("id", src.id);

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
