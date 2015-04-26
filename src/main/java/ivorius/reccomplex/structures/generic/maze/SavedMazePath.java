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
public class SavedMazePath implements NBTCompoundObject
{
    public int pathDimension;
    public MazeRoom sourceRoom;
    public boolean pathGoesUp;

    public final SavedConnector connector = new SavedConnector();

    public SavedMazePath()
    {
        connector.id = ConnectorStrategy.DEFAULT_PATH;
    }

    public SavedMazePath(int pathDimension, MazeRoom sourceRoom, boolean pathGoesUp, String connector)
    {
        this.pathDimension = pathDimension;
        this.sourceRoom = sourceRoom;
        this.pathGoesUp = pathGoesUp;
        this.connector.id = connector;
    }

    public Map.Entry<MazeRoomConnection, Connector> toRoomConnection(ConnectorFactory factory)
    {
        return Pair.of(new MazeRoomConnection(sourceRoom, sourceRoom.addInDimension(pathDimension, pathGoesUp ? 1 : -1)), connector.toConnector(factory));
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        if (compound.hasKey("source", Constants.NBT.TAG_COMPOUND)) // Legacy
            sourceRoom = new MazeRoom(compound.getCompoundTag("source").getIntArray("coordinates"));
        else
            sourceRoom = new MazeRoom(compound.getIntArray("source"));

        pathDimension = compound.getInteger("pathDimension");
        pathGoesUp = compound.getBoolean("pathGoesUp");
        connector.id = compound.hasKey("connector", Constants.NBT.TAG_STRING)
            ? compound.getString("connector")
            : ConnectorStrategy.DEFAULT_PATH;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setTag("source", sourceRoom.storeInNBT());
        compound.setInteger("pathDimension", pathDimension);
        compound.setBoolean("pathGoesUp", pathGoesUp);
        compound.setString("connector", connector.id);
    }

    public static class Serializer implements JsonSerializer<SavedMazePath>, JsonDeserializer<SavedMazePath>
    {
        @Override
        public SavedMazePath deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "MazeRoom");

            MazeRoom src = context.deserialize(jsonObject.get("source"), MazeRoom.class);
            int pathDimension = JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "pathDimension");
            boolean pathGoesUp = JsonUtils.getJsonObjectBooleanFieldValue(jsonObject, "pathGoesUp");
            String connector = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "connector", ConnectorStrategy.DEFAULT_PATH);

            return new SavedMazePath(pathDimension, src, pathGoesUp, connector);
        }

        @Override
        public JsonElement serialize(SavedMazePath src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("source", context.serialize(src.sourceRoom));
            jsonObject.addProperty("pathDimension", src.pathDimension);
            jsonObject.addProperty("pathGoesUp", src.pathGoesUp);
            jsonObject.addProperty("connector", src.connector.id);

            return jsonObject;
        }
    }
}
