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

import java.lang.reflect.Type;

/**
 * Created by lukas on 01.03.16.
 */
public class SavedMazePath implements NBTCompoundObject
{
    public int pathDimension;
    public MazeRoom sourceRoom;
    public boolean pathGoesUp;

    public SavedMazePath()
    {
    }

    public SavedMazePath(int pathDimension, MazeRoom sourceRoom, boolean pathGoesUp)
    {
        this.pathDimension = pathDimension;
        this.sourceRoom = sourceRoom;
        this.pathGoesUp = pathGoesUp;
    }

    public MazeRoomConnection toRoomConnection()
    {
        return new MazeRoomConnection(sourceRoom, sourceRoom.addInDimension(pathDimension, pathGoesUp ? 1 : -1));
    }

    public SavedMazePath copy()
    {
        return new SavedMazePath(pathDimension, sourceRoom, pathGoesUp);
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
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setTag("source", sourceRoom.storeInNBT());
        compound.setInteger("pathDimension", pathDimension);
        compound.setBoolean("pathGoesUp", pathGoesUp);
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

            return new SavedMazePath(pathDimension, src, pathGoesUp);
        }

        @Override
        public JsonElement serialize(SavedMazePath src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("source", context.serialize(src.sourceRoom));
            jsonObject.addProperty("pathDimension", src.pathDimension);
            jsonObject.addProperty("pathGoesUp", src.pathGoesUp);

            return jsonObject;
        }
    }
}
