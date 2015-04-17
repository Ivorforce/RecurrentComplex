/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.*;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.reccomplex.structures.generic.Selection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by lukas on 07.10.14.
 */
public class SavedMazeComponent implements NBTCompoundObject, WeightedSelector.Item
{
    public Double weight;
    public final Selection rooms = new Selection();
    public final List<SavedMazePath> exitPaths = new ArrayList<>();

    public SavedMazeComponent()
    {
    }

    public SavedMazeComponent(Double weight)
    {
        this.weight = weight;
    }

    public boolean isValid()
    {
        return !rooms.isEmpty();
    }

    public Collection<MazeRoom> getRooms()
    {
        return rooms.mazeRooms(true);
    }

    public List<SavedMazePath> getExitPaths()
    {
        return Collections.unmodifiableList(exitPaths);
    }

    public void setExitPaths(List<SavedMazePath> exitPaths)
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
            int[] coordinates = room.getCoordinates();
            for (int i = 0; i < coordinates.length; i++)
            {
                if (coordinates[i] < lowest[i])
                    lowest[i] = coordinates[i];
                else if (coordinates[i] > highest[i])
                    highest[i] = coordinates[i];
            }
        }

        int[] size = IvVecMathHelper.sub(highest, lowest);
        for (int i = 0; i < size.length; i++)
            size[i]++;

        return size;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        weight = compound.hasKey("weight", Constants.NBT.TAG_DOUBLE) ? compound.getDouble("weight") : null;

        if (compound.hasKey("roomArea", Constants.NBT.TAG_COMPOUND))
        {
            rooms.readFromNBT(compound.getCompoundTag("roomArea"), 3);
        }
        else if (compound.hasKey("rooms", Constants.NBT.TAG_LIST))
        {
            // Legacy
            rooms.clear();
            rooms.addAll(Lists.transform(NBTTagLists.compoundsFrom(compound, "rooms"), new Function<NBTTagCompound, Selection.Area>()
            {
                @Nullable
                @Override
                public Selection.Area apply(NBTTagCompound input)
                {
                    MazeRoom room = new MazeRoom(input);
                    int[] coordinates = room.getCoordinates();
                    return new Selection.Area(true, coordinates, coordinates.clone());
                }
            }));
        }

        exitPaths.clear();
        exitPaths.addAll(NBTCompoundObjects.readListFrom(compound, "exits", SavedMazePath.class));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        if (weight != null)
            compound.setDouble("weight", weight);

        NBTTagCompound roomsCompound = new NBTTagCompound();
        rooms.writeToNBT(roomsCompound);
        compound.setTag("rooms", roomsCompound);

        NBTCompoundObjects.writeListTo(compound, "exits", exitPaths);
    }

    @Override
    public double getWeight()
    {
        return weight != null ? weight : 1.0;
    }

    public boolean hasDefaultWeight()
    {
        return weight == null;
    }

    public static class Serializer implements JsonSerializer<SavedMazeComponent>, JsonDeserializer<SavedMazeComponent>
    {
        @Override
        public SavedMazeComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "MazeComponent");

            Double weight = jsonObject.has("weightD") ? JsonUtils.getJsonObjectDoubleFieldValue(jsonObject, "weightD") : null;
            if (weight == null && jsonObject.has("weight")) // legacy
                weight = JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "weight") * 0.01; // 100 was default

            SavedMazeComponent mazeComponent = new SavedMazeComponent(weight);

            if (jsonObject.has("roomArea"))
            {
                mazeComponent.rooms.addAll(context.<Selection>deserialize(jsonObject.get("roomArea"), Selection.class));
            }
            if (jsonObject.has("rooms"))
            {
                // Legacy
                MazeRoom[] rooms = context.deserialize(jsonObject.get("rooms"), MazeRoom[].class);
                for (MazeRoom room : rooms)
                    mazeComponent.rooms.add(new Selection.Area(true, room.getCoordinates(), room.getCoordinates()));
            }

            SavedMazePath[] exits = context.deserialize(jsonObject.get("exits"), SavedMazePath[].class);
            mazeComponent.setExitPaths(Arrays.asList(exits));

            return mazeComponent;
        }

        @Override
        public JsonElement serialize(SavedMazeComponent src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            if (src.weight != null)
                jsonObject.addProperty("weightD", src.weight);

            jsonObject.add("roomArea", context.serialize(src.rooms));
            jsonObject.add("exits", context.serialize(src.exitPaths));

            return jsonObject;
        }
    }

    // Legacy
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

            jsonObject.add("coordinates", context.serialize(src.getCoordinates()));

            return jsonObject;
        }
    }
}
