/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.generation;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.maze.classic.MazeRoom;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceStructureListGeneration;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.world.gen.feature.structure.Placer;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by lukas on 21.02.15.
 */
public class ListGeneration extends GenerationType implements WeightedSelector.Item
{
    public String listID;

    public Double weight;

    public BlockPos shift;

    public EnumFacing front;

    public ListGeneration()
    {
        this(null, "", null, BlockPos.ORIGIN, EnumFacing.NORTH);
    }

    public ListGeneration(@Nullable String id, String listID, Double weight, BlockPos shift, EnumFacing front)
    {
        super(id != null ? id : randomID(ListGeneration.class));
        this.listID = listID;
        this.weight = weight;
        this.shift = shift;
        this.front = front;
    }

    public static Stream<Pair<Structure<?>, ListGeneration>> structures(StructureRegistry registry, final String listID, @Nullable final EnumFacing front)
    {
        final Predicate<Pair<Structure<?>, ListGeneration>> predicate = input -> listID.equals(input.getRight().listID)
                && (front == null || input.getLeft().isRotatable() || input.getRight().front == front);
        return registry.getGenerationTypes(ListGeneration.class).stream().filter(predicate);
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

    public BlockPos getShift()
    {
        return shift;
    }

    public void setShift(BlockPos shift)
    {
        this.shift = shift;
    }

    @Override
    public String displayString()
    {
        return IvTranslations.format("reccomplex.generationInfo.structureList.title", listID);
    }

    @Nullable
    @Override
    public Placer placer()
    {
        return null;
    }

    @Override
    public TableDataSource tableDataSource(Function<MazeRoom, BlockPos> realWorldMapper, TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceStructureListGeneration(navigator, delegate, this);
    }

    @Override
    public double getWeight()
    {
        return weight != null ? weight : 1.0;
    }

    public static class Serializer implements JsonSerializer<ListGeneration>, JsonDeserializer<ListGeneration>
    {
        @Override
        public ListGeneration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "vanillaStructureSpawnInfo");

            String id = readID(jsonObject);

            String listID = JsonUtils.getString(jsonObject, "listID", "");

            Double weight = jsonObject.has("weight") ? JsonUtils.getDouble(jsonObject, "weight") : null;

            int positionX = JsonUtils.getInt(jsonObject, "positionX", 0);
            int positionY = JsonUtils.getInt(jsonObject, "positionY", 0);
            int positionZ = JsonUtils.getInt(jsonObject, "positionZ", 0);

            EnumFacing front = Directions.deserialize(JsonUtils.getString(jsonObject, "front", "NORTH"));

            return new ListGeneration(id, listID, weight, new BlockPos(positionX, positionY, positionZ), front);
        }

        @Override
        public JsonElement serialize(ListGeneration src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            jsonObject.addProperty("listID", src.listID);

            if (src.weight != null)
                jsonObject.addProperty("weight", src.weight);

            jsonObject.addProperty("positionX", src.shift.getX());
            jsonObject.addProperty("positionY", src.shift.getY());
            jsonObject.addProperty("positionZ", src.shift.getZ());

            jsonObject.addProperty("front", Directions.serialize(src.front));

            return jsonObject;
        }
    }
}
