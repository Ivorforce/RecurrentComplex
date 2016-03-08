/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.gson.*;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.maze.components.MazeRoomConnection;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.reccomplex.json.JsonUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by lukas on 14.04.15.
 */
public class SavedMazePathConnection implements NBTCompoundObject
{
    public final SavedMazePath path = new SavedMazePath();
    public final SavedConnector connector = new SavedConnector();

    public SavedMazePathConnection()
    {
        connector.id = ConnectorStrategy.DEFAULT_PATH;
    }

    public SavedMazePathConnection(SavedMazePath path, String connector)
    {
        this(path.pathDimension, path.sourceRoom, path.pathGoesUp, connector);
    }

    public SavedMazePathConnection(int pathDimension, MazeRoom sourceRoom, boolean pathGoesUp, String connector)
    {
        path.pathDimension = pathDimension;
        path.sourceRoom = sourceRoom;
        path.pathGoesUp = pathGoesUp;
        this.connector.id = connector;
    }

    public Map.Entry<MazeRoomConnection, Connector> toRoomConnection(ConnectorFactory factory)
    {
        return Pair.of(path.toRoomConnection(), connector.toConnector(factory));
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

    public static class Serializer implements JsonSerializer<SavedMazePathConnection>, JsonDeserializer<SavedMazePathConnection>
    {
        @Override
        public SavedMazePathConnection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "MazeRoom");

            SavedMazePath path = context.deserialize(json, SavedMazePath.class); // Don't do this, kids.
            String connector = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "connector", ConnectorStrategy.DEFAULT_PATH);

            return new SavedMazePathConnection(path, connector);
        }

        @Override
        public JsonElement serialize(SavedMazePathConnection src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = (JsonObject) context.serialize(src.path); // Don't do this, kids.

            jsonObject.addProperty("connector", src.connector.id);

            return jsonObject;
        }
    }
}
