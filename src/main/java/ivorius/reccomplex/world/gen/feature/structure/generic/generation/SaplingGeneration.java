/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.generation;

import com.google.gson.*;
import ivorius.ivtoolkit.maze.classic.MazeRoom;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceSaplingGeneration;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.Placer;
import ivorius.reccomplex.world.gen.feature.structure.generic.BlockPattern;
import ivorius.reccomplex.utils.expression.EnvironmentExpression;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.GenericPlacer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * Created by lukas on 19.01.15.
 */
public class SaplingGeneration extends GenerationType
{
    public Double generationWeight;

    public BlockPos spawnShift;

    public EnvironmentExpression environmentExpression;

    @Nonnull
    public BlockPattern pattern;

    public SaplingGeneration()
    {
        this(null, null, BlockPos.ORIGIN, "", new BlockPattern());
    }

    public SaplingGeneration(@Nullable String id, Double generationWeight, BlockPos spawnShift, String environmentExpression, BlockPattern pattern)
    {
        super(id != null ? id : randomID(SaplingGeneration.class));
        this.generationWeight = generationWeight;
        this.spawnShift = spawnShift;
        this.environmentExpression = ExpressionCache.of(new EnvironmentExpression(), environmentExpression);
        this.pattern = pattern;
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

    public void setSpawnShift(BlockPos spawnShift)
    {
        this.spawnShift = spawnShift;
    }

    @Override
    public String displayString()
    {
        return IvTranslations.get("reccomplex.generationInfo.sapling.title");
    }

    @Nullable
    @Override
    public Placer placer()
    {
        return GenericPlacer.surfacePlacer();
    }

    @Override
    public TableDataSource tableDataSource(Function<MazeRoom, BlockPos> realWorldMapper, TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceSaplingGeneration(this, realWorldMapper, navigator, delegate);
    }

    public boolean generatesIn(Environment environment)
    {
        return environmentExpression.test(environment);
    }

    @Nonnull
    public BlockPattern getPattern()
    {
        return pattern;
    }

    public double getActiveWeight()
    {
        return generationWeight != null ? generationWeight : 1.0;
    }

    public static class Serializer implements JsonSerializer<SaplingGeneration>, JsonDeserializer<SaplingGeneration>
    {
        @Override
        public SaplingGeneration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "vanillaStructureSpawnInfo");

            String id = readID(jsonObject);

            Double spawnWeight = jsonObject.has("generationWeight") ? JsonUtils.getDouble(jsonObject, "generationWeight") : null;

            int spawnX = JsonUtils.getInt(jsonObject, "spawnShiftX", 0);
            int spawnY = JsonUtils.getInt(jsonObject, "spawnShiftY", 0);
            int spawnZ = JsonUtils.getInt(jsonObject, "spawnShiftZ", 0);

            String environmentExpression = JsonUtils.getString(jsonObject, "environmentExpression", "");
            BlockPattern pattern = BlockPattern.gson.fromJson(JsonUtils.getJsonObject(jsonObject, "pattern", new JsonObject()), BlockPattern.class);

            return new SaplingGeneration(id, spawnWeight, new BlockPos(spawnX, spawnY, spawnZ), environmentExpression, pattern);
        }

        @Override
        public JsonElement serialize(SaplingGeneration src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            if (src.generationWeight != null)
                jsonObject.addProperty("generationWeight", src.generationWeight);

            jsonObject.addProperty("spawnShiftX", src.spawnShift.getX());
            jsonObject.addProperty("spawnShiftY", src.spawnShift.getY());
            jsonObject.addProperty("spawnShiftZ", src.spawnShift.getZ());

            jsonObject.addProperty("environmentExpression", src.environmentExpression.getExpression());
            jsonObject.add("pattern", BlockPattern.gson.toJsonTree(src.pattern));

            return jsonObject;
        }
    }
}
