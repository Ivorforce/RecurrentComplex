/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by lukas on 07.10.14.
 */
public class SavedMazeComponent implements NBTCompoundObject
{
    public final Selection rooms = new Selection(3);
    public final List<SavedMazePathConnection> exitPaths = new ArrayList<>();
    public final SavedConnector defaultConnector = new SavedConnector(ConnectorStrategy.DEFAULT_WALL);
    public final SavedMazeReachability reachability = new SavedMazeReachability();

    public SavedMazeComponent()
    {
        rooms.add(new Selection.Area(true, new int[3], new int[3]));
    }

    public SavedMazeComponent(String defaultConnector)
    {
        this.defaultConnector.id = defaultConnector;
    }

    public boolean isValid()
    {
        return !rooms.isEmpty();
    }

    public Collection<MazeRoom> getRooms()
    {
        return rooms.compile(true).keySet();
    }

    public List<SavedMazePathConnection> getExitPaths()
    {
        return Collections.unmodifiableList(exitPaths);
    }

    public void setExitPaths(List<SavedMazePathConnection> exitPaths)
    {
        this.exitPaths.clear();
        this.exitPaths.addAll(exitPaths);
    }

    public int[] boundsSize()
    {
        return rooms.boundsSize();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        if (compound.hasKey("roomArea", Constants.NBT.TAG_COMPOUND))
        {
            rooms.readFromNBT(compound.getCompoundTag("roomArea"));
        }
        else if (compound.hasKey("rooms", Constants.NBT.TAG_LIST)) // Legacy
        {
            rooms.clear();
            rooms.addAll(Lists.transform(NBTTagLists.compoundsFrom(compound, "rooms"), input -> {
                MazeRoom room = new MazeRoom(IvNBTHelper.readIntArrayFixedSize("coordinates", 3, compound));
                int[] coordinates = room.getCoordinates();
                return new Selection.Area(true, coordinates, coordinates.clone());
            }));
        }

        exitPaths.clear();
        exitPaths.addAll(NBTCompoundObjects.readListFrom(compound, "exits", SavedMazePathConnection.class));

        defaultConnector.id = compound.hasKey("defaultConnector", Constants.NBT.TAG_STRING)
                ? compound.getString("defaultConnector")
                : ConnectorStrategy.DEFAULT_PATH;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setTag("roomArea", NBTCompoundObjects.write(rooms));

        NBTCompoundObjects.writeListTo(compound, "exits", exitPaths);

        compound.setString("defaultConnector", defaultConnector.id);
    }

    public static class Serializer implements JsonSerializer<SavedMazeComponent>, JsonDeserializer<SavedMazeComponent>
    {
        @Override
        public SavedMazeComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "MazeComponent");

            String defaultConnector = JsonUtils.getString(jsonObject, "defaultConnector", ConnectorStrategy.DEFAULT_WALL);

            SavedMazeComponent mazeComponent = new SavedMazeComponent(defaultConnector);

            if (jsonObject.has("roomArea"))
            {
                mazeComponent.rooms.addAll(context.deserialize(jsonObject.get("roomArea"), new TypeToken<List<Selection.Area>>(){}.getType()));
            }
            if (jsonObject.has("rooms"))
            {
                // Legacy
                MazeRoom[] rooms = context.deserialize(jsonObject.get("rooms"), MazeRoom[].class);
                for (MazeRoom room : rooms)
                    mazeComponent.rooms.add(new Selection.Area(true, room.getCoordinates(), room.getCoordinates()));
            }

            SavedMazePathConnection[] exits = context.deserialize(jsonObject.get("exits"), SavedMazePathConnection[].class);
            mazeComponent.setExitPaths(Arrays.asList(exits));

            if (jsonObject.has("reachability"))
                mazeComponent.reachability.set(context.deserialize(jsonObject.get("reachability"), SavedMazeReachability.class));

            return mazeComponent;
        }

        @Override
        public JsonElement serialize(SavedMazeComponent src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("roomArea", context.serialize(src.rooms));
            jsonObject.add("exits", context.serialize(src.exitPaths));

            jsonObject.addProperty("defaultConnector", src.defaultConnector.id);

            jsonObject.add("reachability", context.serialize(src.reachability));

            return jsonObject;
        }
    }

    // Legacy
    public static class RoomSerializer implements JsonSerializer<MazeRoom>, JsonDeserializer<MazeRoom>
    {
        @Override
        public MazeRoom deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "MazeRoom");

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
