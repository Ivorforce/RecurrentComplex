/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.gson.*;
import ivorius.ivtoolkit.maze.components.MazePassage;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.reccomplex.json.JsonUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

/**
 * Created by lukas on 01.03.16.
 */
public class SavedMazePath implements NBTCompoundObject, Comparable<SavedMazePath>
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

    public void set(SavedMazePath path)
    {
        pathDimension = path.pathDimension;
        sourceRoom = path.sourceRoom;
        pathGoesUp = path.pathGoesUp;
    }

    public MazePassage build()
    {
        return new MazePassage(getSourceRoom(), getDestRoom());
    }

    public MazeRoom getSourceRoom()
    {
        return sourceRoom;
    }

    public MazeRoom getDestRoom()
    {
        return sourceRoom.addInDimension(pathDimension, pathGoesUp ? 1 : -1);
    }

    public SavedMazePath inverse()
    {
        return new SavedMazePath(pathDimension, sourceRoom.addInDimension(pathDimension, pathGoesUp ? 1 : -1), !pathGoesUp);
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

    @Override
    public String toString()
    {
        return String.format("%s %s", sourceRoom, getEnumFacing());
    }

    public EnumFacing getEnumFacing()
    {
        switch (pathDimension)
        {
            case 0:
                return pathGoesUp ? EnumFacing.EAST : EnumFacing.WEST;
            case 1:
                return pathGoesUp ? EnumFacing.UP : EnumFacing.DOWN;
            case 2:
                return pathGoesUp ? EnumFacing.SOUTH : EnumFacing.NORTH;
        }

        throw new InternalError();
    }

    @Override
    public int compareTo(@Nonnull SavedMazePath o)
    {
        return toString().compareTo(o.toString()); // lol
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SavedMazePath that = (SavedMazePath) o;

        if (pathDimension != that.pathDimension) return false;
        if (pathGoesUp != that.pathGoesUp) return false;
        return sourceRoom != null ? sourceRoom.equals(that.sourceRoom) : that.sourceRoom == null;

    }

    @Override
    public int hashCode()
    {
        int result = pathDimension;
        result = 31 * result + (sourceRoom != null ? sourceRoom.hashCode() : 0);
        result = 31 * result + (pathGoesUp ? 1 : 0);
        return result;
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
