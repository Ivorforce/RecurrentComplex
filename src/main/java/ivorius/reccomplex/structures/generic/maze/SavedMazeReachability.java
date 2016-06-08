/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.maze.components.*;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.scripts.world.WorldScriptMazeGenerator;
import ivorius.reccomplex.structures.generic.Selection;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by lukas on 18.01.16.
 */
public class SavedMazeReachability implements NBTCompoundObject
{
    private static final Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

    public final List<Set<SavedMazePath>> groups = new ArrayList<>();
    public final List<ImmutablePair<SavedMazePath, SavedMazePath>> crossConnections = new ArrayList<>();

    public <T extends Map.Entry<SavedMazePath, SavedMazePath>> SavedMazeReachability(List<Set<SavedMazePath>> groups, List<T> crossConnections)
    {
        set(groups, crossConnections);
    }

    public SavedMazeReachability()
    {
    }


    public static Predicate<MazePassage> notBlocked(final Collection<Connector> blockedConnections, final Map<MazePassage, Connector> connections)
    {
        return input -> !blockedConnections.contains(connections.get(input));
    }

    public static Set<SavedMazePath> buildExpected(WorldScriptMazeGenerator script)
    {
        Set<SavedMazePath> complete = Sets.newHashSet(script.exitPaths.stream().map(input -> input.path).collect(Collectors.toList()));
        completeExitPaths(complete, script.rooms);
        return complete;
    }

    public static Set<SavedMazePath> buildExpected(SavedMazeComponent savedMazeComponent)
    {
        Set<SavedMazePath> complete = Sets.newHashSet(savedMazeComponent.exitPaths.stream().map(input -> input.path).collect(Collectors.toList()));
        completeExitPaths(complete, savedMazeComponent.rooms);
        return complete;
    }

    /**
     * Analogous to WorldGenMaze.completeExitPaths
     * @param exits
     * @param rooms
     */
    public static void completeExitPaths(Set<SavedMazePath> exits, Selection rooms)
    {
        Set<MazeRoom> roomSet = rooms.mazeRooms(true);
        for (MazeRoom room : roomSet)
            SavedMazePaths.neighborPaths(room).filter(connection -> !exits.contains(connection) && !(roomSet.contains(connection.getSourceRoom()) && roomSet.contains(connection.getDestRoom()))).forEach(exits::add);
    }

    public void set(SavedMazeReachability reachability)
    {
        set(reachability.groups, reachability.crossConnections);
    }

    public <T extends Map.Entry<SavedMazePath, SavedMazePath>> void set(List<Set<SavedMazePath>> groups, List<T> crossConnections)
    {
        this.groups.clear();
        for (Set<SavedMazePath> group : groups)
            this.groups.add(Sets.newHashSet(group.stream().map(SavedMazePath::copy).collect(Collectors.toList())));

        this.crossConnections.clear();
        for (Map.Entry<SavedMazePath, SavedMazePath> entry : crossConnections)
            this.crossConnections.add(ImmutablePair.of(entry.getKey().copy(), entry.getValue().copy()));
    }

    public ImmutableMultimap<MazePassage, MazePassage> build(final AxisAlignedTransform2D transform, final int[] size, Predicate<MazePassage> filter, Set<MazePassage> connections)
    {
        filter = ((Predicate<MazePassage>) Predicates.in(connections)::apply).and(filter);

        ImmutableMultimap.Builder<MazePassage, MazePassage> builder = ImmutableSetMultimap.builder();
        Set<MazePassage> defaultGroup = Sets.newHashSet(connections);

        for (Set<SavedMazePath> group : groups)
        {
            List<MazePassage> mazePassages = group.stream().map(savedMazePath -> MazePassages.rotated(savedMazePath.build(), transform, size)).filter(filter).collect(Collectors.toList());
            defaultGroup.removeAll(mazePassages);
            addInterconnections(builder, mazePassages.stream());
        }

        addInterconnections(builder, defaultGroup.stream());

        for (Map.Entry<SavedMazePath, SavedMazePath> entry : crossConnections)
        {
            MazePassage key = MazePassages.rotated(entry.getKey().build(), transform, size);
            MazePassage val = MazePassages.rotated(entry.getValue().build(), transform, size);

            if (filter.test(key) && filter.test(val))
                builder.put(key, val);
        }

        return builder.build();
    }

    protected void addInterconnections(ImmutableMultimap.Builder<MazePassage, MazePassage> builder, Stream<MazePassage> existing)
    {
        existing.reduce((last, current) -> {
            if (last != null) // It's enough to make a transitive connection in both directions
            {
                builder.put(last, current);
                builder.put(current, last);
            }
            return current;
        });
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        groups.clear();
        groups.addAll(Lists.transform(NBTTagLists.listsFrom(compound, "groups"), input -> Sets.newHashSet(NBTCompoundObjects.readList(input, SavedMazePath.class))));

        crossConnections.clear();
        crossConnections.addAll(Lists.transform(NBTTagLists.compoundsFrom(compound, "crossConnections"), input -> ImmutablePair.of(NBTCompoundObjects.readFrom(input, "key", SavedMazePath.class), NBTCompoundObjects.readFrom(input, "val", SavedMazePath.class))));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagLists.writeTo(compound, "groups", Lists.transform(groups, NBTCompoundObjects::writeList));

        NBTTagLists.writeCompoundsTo(compound, "crossConnections", Lists.transform(crossConnections, input -> {
            NBTTagCompound compound1 = new NBTTagCompound();
            NBTCompoundObjects.writeTo(compound1, "key", input.getKey());
            NBTCompoundObjects.writeTo(compound1, "val", input.getValue());
            return compound1;
        }));
    }

    public static class Serializer implements JsonSerializer<SavedMazeReachability>, JsonDeserializer<SavedMazeReachability>
    {
        @Override
        public SavedMazeReachability deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "MazeReachability");

            List<Set<SavedMazePath>> groups = context.deserialize(jsonObject.get("groups"), new TypeToken<List<Set<SavedMazePath>>>(){}.getType());
            if (groups == null)
                groups = Collections.emptyList();

            List<ImmutablePair<SavedMazePath, SavedMazePath>> crossConnections = gson.fromJson(JsonUtils.getJsonObjectJsonArrayFieldOrDefault(jsonObject, "crossConnections", new JsonArray()), new TypeToken<List<ImmutablePair<SavedMazePath, SavedMazePath>>>(){}.getType());
            if (crossConnections == null)
                crossConnections = Collections.emptyList();

            return new SavedMazeReachability(groups, crossConnections);
        }

        @Override
        public JsonElement serialize(SavedMazeReachability src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("groups", context.serialize(src.groups));
            jsonObject.add("crossConnections", gson.toJsonTree(src.crossConnections));

            return jsonObject;
        }
    }
}
