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
import java.util.*;

/**
 * Created by lukas on 07.10.14.
 */
public class SavedMazeComponent extends WeightedRandom.Item
{
    public final Selection rooms = Selection.zeroSelection(3);
    public final List<MazePath> exitPaths = new ArrayList<>();

    public SavedMazeComponent(int weight)
    {
        super(weight);
    }

    public SavedMazeComponent(NBTTagCompound compound)
    {
        super(compound.getInteger("weight"));

        if (compound.hasKey("roomArea", Constants.NBT.TAG_COMPOUND))
        {
            rooms.readFromNBT(compound.getCompoundTag("roomArea"), 3);
        }
        else if (compound.hasKey("rooms", Constants.NBT.TAG_LIST))
        {
            // Legacy
            NBTTagList roomsList = compound.getTagList("rooms", Constants.NBT.TAG_COMPOUND);
            rooms.clear();
            for (int i = 0; i < roomsList.tagCount(); i++)
            {
                MazeRoom room = new MazeRoom(roomsList.getCompoundTagAt(i));
                rooms.add(new Selection.Area(true, room.coordinates, room.coordinates.clone()));
            }
        }

        NBTTagList exitsList = compound.getTagList("exits", Constants.NBT.TAG_COMPOUND);
        exitPaths.clear();
        for (int i = 0; i < exitsList.tagCount(); i++)
            exitPaths.add(new MazePath(exitsList.getCompoundTagAt(i)));
    }

    public boolean isValid()
    {
        return !rooms.isEmpty();
    }

    public Collection<MazeRoom> getRooms()
    {
        return rooms.mazeRooms(true);
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
        int[] lowest = rooms.get(0).getMinCoord();
        int[] highest = rooms.get(0).getMaxCoord();
        for (MazeRoom room : getRooms())
        {
            for (int i = 0; i < room.coordinates.length; i++)
            {
                if (room.coordinates[i] < lowest[i])
                    lowest[i] = room.coordinates[i];
                else if (room.coordinates[i] > highest[i])
                    highest[i] = room.coordinates[i];
            }
        }

        int[] size = IvVecMathHelper.sub(highest, lowest);
        for (int i = 0; i < size.length; i++)
            size[i]++;

        return size;
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setInteger("weight", itemWeight);

        NBTTagCompound roomsCompound = new NBTTagCompound();
        rooms.writeToNBT(roomsCompound);
        compound.setTag("rooms", roomsCompound);

        NBTTagList exitsList = new NBTTagList();
        for (MazePath exit : exitPaths)
            exitsList.appendTag(exit.writeToNBT());
        compound.setTag("exits", exitsList);
    }

    public static class Serializer implements JsonSerializer<SavedMazeComponent>, JsonDeserializer<SavedMazeComponent>
    {
        @Override
        public SavedMazeComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "MazeComponent");

            SavedMazeComponent mazeComponent = new SavedMazeComponent(JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "weight"));

            if (jsonObject.has("roomArea"))
            {
                mazeComponent.rooms.addAll((Selection) context.deserialize(jsonObject.get("roomArea"), Selection.class));
            }
            if (jsonObject.has("rooms"))
            {
                // Legacy
                MazeRoom[] rooms = context.deserialize(jsonObject.get("rooms"), MazeRoom[].class);
                for (MazeRoom room : rooms)
                    mazeComponent.rooms.add(new Selection.Area(true, room.coordinates, room.coordinates.clone()));

                MazePath[] exits = context.deserialize(jsonObject.get("exits"), MazePath[].class);
                mazeComponent.setExitPaths(Arrays.asList(exits));
            }

            return mazeComponent;
        }

        @Override
        public JsonElement serialize(SavedMazeComponent src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("weight", src.itemWeight);
            jsonObject.add("roomArea", context.serialize(src.rooms));
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
