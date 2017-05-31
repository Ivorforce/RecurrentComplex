/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ivorius.ivtoolkit.maze.components.MazePassage;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.EnvironmentExpression;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by lukas on 14.04.15.
 */
public class SavedMazePathConnection implements NBTCompoundObject
{
    public final SavedMazePath path = new SavedMazePath();
    public final SavedConnector connector = new SavedConnector(ConnectorStrategy.DEFAULT_PATH);
    public final List<ConditionalConnector> conditionalConnectors = new ArrayList<>();

    public SavedMazePathConnection()
    {
    }

    public SavedMazePathConnection(SavedMazePath path, SavedConnector connector, List<ConditionalConnector> conditionalConnectors)
    {
        this(path.pathDimension, path.sourceRoom, path.pathGoesUp, connector.id, conditionalConnectors);
    }

    public SavedMazePathConnection(int pathDimension, MazeRoom sourceRoom, boolean pathGoesUp, String connector, List<ConditionalConnector> conditionalConnectors)
    {
        path.pathDimension = pathDimension;
        path.sourceRoom = sourceRoom;
        path.pathGoesUp = pathGoesUp;
        this.connector.id = connector;
        this.conditionalConnectors.addAll(conditionalConnectors);
    }

    public SavedMazePath getPath()
    {
        return path;
    }

    public SavedConnector getConnector()
    {
        return connector;
    }

    public Map.Entry<MazePassage, Connector> build(Environment environment, ConnectorFactory factory)
    {
        SavedConnector connector = this.connector;
        for (ConditionalConnector conditionalConnector : conditionalConnectors)
        {
            if (conditionalConnector.expression.test(environment))
            {
                connector = conditionalConnector.connector;
                break;
            }
        }
        return Pair.of(path.build(), connector.toConnector(factory));
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        path.readFromNBT(compound);
        connector.id = compound.hasKey("connector", Constants.NBT.TAG_STRING)
                ? compound.getString("connector")
                : ConnectorStrategy.DEFAULT_PATH;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        path.writeToNBT(compound);
        compound.setString("connector", connector.id);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SavedMazePathConnection that = (SavedMazePathConnection) o;

        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (connector != null ? !connector.equals(that.connector) : that.connector != null) return false;
        return conditionalConnectors != null ? conditionalConnectors.equals(that.conditionalConnectors) : that.conditionalConnectors == null;
    }

    @Override
    public int hashCode()
    {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (connector != null ? connector.hashCode() : 0);
        result = 31 * result + (conditionalConnectors != null ? conditionalConnectors.hashCode() : 0);
        return result;
    }

    public SavedMazePathConnection copy()
    {
        return new SavedMazePathConnection(path.copy(), connector.copy(),
                conditionalConnectors.stream().map(ConditionalConnector::copy).collect(Collectors.toList()));
    }

    public static class Serializer implements JsonSerializer<SavedMazePathConnection>, JsonDeserializer<SavedMazePathConnection>
    {
        @Override
        public SavedMazePathConnection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "MazeRoom");

            SavedMazePath path = context.deserialize(json, SavedMazePath.class); // Don't do this, kids.
            String connector = JsonUtils.getString(jsonObject, "connector", ConnectorStrategy.DEFAULT_PATH);
            List<ConditionalConnector> conditionalConnectors = context.deserialize(JsonUtils.getJsonArray(jsonObject, "conditionalConnectors", new JsonArray()), new TypeToken<List<ConditionalConnector>>() {}.getType());

            return new SavedMazePathConnection(path, new SavedConnector(connector), conditionalConnectors);
        }

        @Override
        public JsonElement serialize(SavedMazePathConnection src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = (JsonObject) context.serialize(src.path); // Don't do this, kids.

            jsonObject.addProperty("connector", src.connector.id);
            jsonObject.add("conditionalConnectors", context.serialize(src.conditionalConnectors));

            return jsonObject;
        }
    }

    public static class ConditionalConnector
    {
        public EnvironmentExpression expression;
        public final SavedConnector connector = new SavedConnector(ConnectorStrategy.DEFAULT_PATH);

        public ConditionalConnector(String expression, String connector)
        {
            this.expression = ExpressionCache.of(new EnvironmentExpression(), expression);
            this.connector.id = connector;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConditionalConnector that = (ConditionalConnector) o;

            if (expression != null ? !expression.equals(that.expression) : that.expression != null) return false;
            return connector != null ? connector.equals(that.connector) : that.connector == null;
        }

        @Override
        public int hashCode()
        {
            int result = expression != null ? expression.hashCode() : 0;
            result = 31 * result + (connector != null ? connector.hashCode() : 0);
            return result;
        }

        public ConditionalConnector copy()
        {
            return new ConditionalConnector(expression.getExpression(), connector.id);
        }

        public static class Serializer implements JsonSerializer<ConditionalConnector>, JsonDeserializer<ConditionalConnector>
        {
            @Override
            public ConditionalConnector deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                JsonObject jsonObject = JsonUtils.asJsonObject(json, "ConditionalConnector");
                return new ConditionalConnector(JsonUtils.getString(jsonObject, "expression", ""),
                        JsonUtils.getString(jsonObject, "connector", ConnectorStrategy.DEFAULT_PATH));
            }

            @Override
            public JsonElement serialize(ConditionalConnector src, Type typeOfSrc, JsonSerializationContext context)
            {
                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("expression", src.expression.getExpression());
                jsonObject.addProperty("connector", src.connector.id);

                return jsonObject;
            }
        }
    }
}
