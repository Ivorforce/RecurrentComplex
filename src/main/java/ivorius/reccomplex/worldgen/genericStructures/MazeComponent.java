/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.genericStructures;

import com.google.gson.*;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.MazePath;
import ivorius.ivtoolkit.maze.MazeRoom;
import ivorius.reccomplex.json.JsonUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.util.Constants;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 07.10.14.
 */
public class MazeComponent extends WeightedRandom.Item
{
    private final List<MazeRoom> rooms = new ArrayList<>();
    private final List<MazePath> exitPaths = new ArrayList<>();

    public MazeComponent(int weight)
    {
        super(weight);
    }

    public MazeComponent(NBTTagCompound compound)
    {
        super(compound.getInteger("weight"));

        NBTTagList roomsList = compound.getTagList("rooms", Constants.NBT.TAG_COMPOUND);
        rooms.clear();
        for (int i = 0; i < roomsList.tagCount(); i++)
            rooms.add(new MazeRoom(roomsList.getCompoundTagAt(i)));

        NBTTagList exitsList = compound.getTagList("exits", Constants.NBT.TAG_COMPOUND);
        exitPaths.clear();
        for (int i = 0; i < exitsList.tagCount(); i++)
            exitPaths.add(new MazePath(exitsList.getCompoundTagAt(i)));
    }

    public List<MazeRoom> getRooms()
    {
        return Collections.unmodifiableList(rooms);
    }

    public void setRooms(List<MazeRoom> rooms)
    {
        this.rooms.clear();
        this.rooms.addAll(rooms);
    }

    public List<MazePath> getExitPaths()
    {
        return Collections.unmodifiableList(exitPaths);
    }

    public void setExitPaths(List<MazePath> exitPaths)
    {
        this.exitPaths.clear();
        this.exitPaths.addAll(exitPaths);
    }

    public int[] getSize()
    {
        int[] lowest = rooms.get(0).coordinates.clone();
        int[] highest = rooms.get(0).coordinates.clone();
        for (MazeRoom room : rooms)
        {
            for (int i = 0; i < room.coordinates.length; i++)
            {
                if (room.coordinates[i] < lowest[i])
                {
                    lowest[i] = room.coordinates[i];
                }
                else if (room.coordinates[i] > highest[i])
                {
                    highest[i] = room.coordinates[i];
                }
            }
        }

        int[] size = IvVecMathHelper.sub(highest, lowest);
        for (int i = 0; i < size.length; i++)
        {
            size[i]++;
        }

        return size;
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setInteger("weight", itemWeight);

        NBTTagList roomsList = new NBTTagList();
        for (MazeRoom room : rooms)
            roomsList.appendTag(room.writeToNBT());
        compound.setTag("rooms", roomsList);

        NBTTagList exitsList = new NBTTagList();
        for (MazePath exit : exitPaths)
            exitsList.appendTag(exit.writeToNBT());
        compound.setTag("exits", exitsList);
    }

    public static class Serializer implements JsonSerializer<MazeComponent>, JsonDeserializer<MazeComponent>
    {
        @Override
        public MazeComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "MazeComponent");

            MazeComponent mazeComponent = new MazeComponent(JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "weight"));

            MazeRoom[] rooms = context.deserialize(jsonObject.get("rooms"), MazeRoom[].class);
            mazeComponent.setRooms(Arrays.asList(rooms));

            MazePath[] exits = context.deserialize(jsonObject.get("exits"), MazePath[].class);
            mazeComponent.setExitPaths(Arrays.asList(exits));

            return mazeComponent;
        }

        @Override
        public JsonElement serialize(MazeComponent src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("weight", src.itemWeight);
            jsonObject.add("rooms", context.serialize(src.rooms));
            jsonObject.add("exits", context.serialize(src.exitPaths));

            return jsonObject;
        }
    }

    public static class RoomSerializer implements JsonSerializer<MazeRoom>, JsonDeserializer<MazeRoom>
    {
        @Override
        public MazeRoom deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "MazeRoom");

            return new MazeRoom(context.<int[]>deserialize(jsonObject.get("coordinates"), int[].class));
        }

        @Override
        public JsonElement serialize(MazeRoom src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("coordinates", context.serialize(src.coordinates));

            return jsonObject;
        }
    }

    public static class PathSerializer implements JsonSerializer<MazePath>, JsonDeserializer<MazePath>
    {
        @Override
        public MazePath deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "MazeRoom");

            MazeRoom src = context.deserialize(jsonObject.get("source"), MazeRoom.class);
            int pathDimension = JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "pathDimension");
            boolean pathGoesUp = JsonUtils.getJsonObjectBooleanFieldValue(jsonObject, "pathGoesUp");

            return new MazePath(src, pathDimension, pathGoesUp);
        }

        @Override
        public JsonElement serialize(MazePath src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("source", context.serialize(src.sourceRoom));
            jsonObject.addProperty("pathDimension", src.pathDimension);
            jsonObject.addProperty("pathGoesUp", src.pathGoesUp);

            return jsonObject;
        }
    }
}
