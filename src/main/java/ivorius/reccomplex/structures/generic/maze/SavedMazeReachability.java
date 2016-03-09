/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.maze.components.MazeRoomConnection;
import ivorius.ivtoolkit.maze.components.MazeRoomConnections;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.utils.NBTCompoundObjects2;
import ivorius.reccomplex.utils.NBTTagLists2;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by lukas on 18.01.16.
 */
public class SavedMazeReachability implements NBTCompoundObject
{
    private static final Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

    // TODO Make GUI
    public final List<List<SavedMazePath>> groups = new ArrayList<>();
    public final Map<SavedMazePath, SavedMazePath> crossConnections = new HashMap<>();

    public SavedMazeReachability(List<List<SavedMazePath>> groups, Map<SavedMazePath, SavedMazePath> crossConnections)
    {
        this.groups.addAll(Lists.transform(groups, new Function<List<SavedMazePath>, List<SavedMazePath>>()
        {
            @Nullable
            @Override
            public List<SavedMazePath> apply(@Nullable List<SavedMazePath> input)
            {
                return Lists.newArrayList(Lists.transform(input, new Function<SavedMazePath, SavedMazePath>()
                {
                    @Nullable
                    @Override
                    public SavedMazePath apply(@Nullable SavedMazePath input)
                    {
                        return input.copy();
                    }
                }));
            }
        }));
        for (Map.Entry<SavedMazePath, SavedMazePath> entry : crossConnections.entrySet())
        {
            this.crossConnections.put(entry.getKey().copy(), entry.getValue().copy());
        }
    }

    public SavedMazeReachability()
    {
    }


    public static Predicate<MazeRoomConnection> notBlocked(final Collection<Connector> blockedConnections, final Map<MazeRoomConnection, Connector> connections)
    {
        return new Predicate<MazeRoomConnection>()
        {
            @Override
            public boolean apply(@Nullable MazeRoomConnection input)
            {
                return !blockedConnections.contains(connections.get(input));
            }
        };
    }

    public void set(SavedMazeReachability reachability)
    {
        groups.clear();

        for (List<SavedMazePath> group : reachability.groups)
            groups.add(Lists.newArrayList(Lists.transform(group, new Function<SavedMazePath, SavedMazePath>()
            {
                @Nullable
                @Override
                public SavedMazePath apply(@Nullable SavedMazePath input)
                {
                    return input.copy();
                }
            })));

        crossConnections.clear();
        crossConnections.putAll(reachability.crossConnections);
    }

    public ImmutableSet<Pair<MazeRoomConnection, MazeRoomConnection>> build(final AxisAlignedTransform2D transform, final int[] size, Predicate<MazeRoomConnection> filter, Set<MazeRoomConnection> connections)
    {
        filter = Predicates.and(Predicates.in(connections), filter);

        ImmutableSet.Builder<Pair<MazeRoomConnection, MazeRoomConnection>> builder = ImmutableSet.builder();
        Set<MazeRoomConnection> defaultGroup = Sets.newHashSet(connections);

        for (List<SavedMazePath> group : groups)
        {
            FluentIterable<MazeRoomConnection> existing = FluentIterable.from(group).transform(new Function<SavedMazePath, MazeRoomConnection>()
            {
                @Nullable
                @Override
                public MazeRoomConnection apply(@Nullable SavedMazePath savedMazePath)
                {
                    return MazeRoomConnections.rotated(savedMazePath.toRoomConnection(), transform, size);
                }
            }).filter(filter);

            for (MazeRoomConnection left : existing)
                defaultGroup.remove(left);

            addInterconnections(builder, existing);
        }

        addInterconnections(builder, defaultGroup);

        for (Map.Entry<SavedMazePath, SavedMazePath> entry : crossConnections.entrySet())
        {
            MazeRoomConnection key = MazeRoomConnections.rotated(entry.getKey().toRoomConnection(), transform, size);
            MazeRoomConnection val = MazeRoomConnections.rotated(entry.getValue().toRoomConnection(), transform, size);

            if (filter.apply(key) && filter.apply(val))
                builder.add(Pair.of(key, val));
        }

        return builder.build();
    }

    protected void addInterconnections(ImmutableSet.Builder<Pair<MazeRoomConnection, MazeRoomConnection>> builder, Iterable<MazeRoomConnection> existing)
    {
        for (MazeRoomConnection left : existing)
            for (MazeRoomConnection right : existing)
                builder.add(Pair.of(left, right));
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        groups.clear();
        groups.addAll(Lists.transform(NBTTagLists2.listsFrom(compound, "groups"), new Function<NBTTagList, List<SavedMazePath>>()
        {
            @Nullable
            @Override
            public List<SavedMazePath> apply(@Nullable NBTTagList input)
            {
                return NBTCompoundObjects.readList(input, SavedMazePath.class);
            }
        }));

        crossConnections.clear();
        crossConnections.putAll(NBTCompoundObjects2.readMapFrom(compound, "crossConnections", SavedMazePath.class, SavedMazePath.class));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagLists2.writeNbt(compound, "groups", Lists.transform(groups, new Function<List<SavedMazePath>, NBTTagList>()
        {
            @Nullable
            @Override
            public NBTTagList apply(@Nullable List<SavedMazePath> input)
            {
                return NBTCompoundObjects.writeList(input);
            }
        }));

        NBTCompoundObjects2.writeMapTo(compound, "crossConnections", crossConnections);
    }

    public static class Serializer implements JsonSerializer<SavedMazeReachability>, JsonDeserializer<SavedMazeReachability>
    {
        @Override
        public SavedMazeReachability deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(json, "MazeRoom");

            List<List<SavedMazePath>> groups = context.deserialize(jsonObject.get("groups"), new TypeToken<List<List<SavedMazePath>>>(){}.getType());
            if (groups == null)
                groups = Collections.emptyList();

            Map<SavedMazePath, SavedMazePath> crossConnections = (Map<SavedMazePath, SavedMazePath>) gson.fromJson(jsonObject.get("crossConnections"), new TypeToken<Map<SavedMazePath, SavedMazePath>>(){}.getType());
            if (crossConnections == null)
                crossConnections = Collections.emptyMap();

            return new SavedMazeReachability(groups, crossConnections);
        }

        @Override
        public JsonElement serialize(SavedMazeReachability src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("source", context.serialize(src.groups));
            jsonObject.addProperty("pathDimension", gson.toJson(src.crossConnections));

            return jsonObject;
        }
    }
}
