/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.common.collect.ImmutableSet;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import ivorius.ivtoolkit.maze.components.MazePassage;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.Ranges;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by lukas on 14.04.15.
 */
public class SavedMazePaths
{
    public static Function<SavedMazePathConnection, Map.Entry<MazePassage, Connector>> buildFunction(final ConnectorFactory factory)
    {
        return input -> input != null ? input.build(factory) : null;
    }

    public static <K, V> void put(Map<K, V> map, Map.Entry<K, V> entry)
    {
        map.put(entry.getKey(), entry.getValue());
    }
    
    public static <K, V> void putAll(Map<K, V> map, Iterable<Map.Entry<K, V>> entries)
    {
        for (Map.Entry<K, V> entry : entries)
            map.put(entry.getKey(), entry.getValue());
    }

    /**
     * Analogous to MazeRooms.neighbors
     * @param room
     * @param dimensions
     * @return
     */
    public static Set<SavedMazePath> neighbors(final MazeRoom room, TIntSet dimensions)
    {
        final ImmutableSet.Builder<SavedMazePath> set = ImmutableSet.builder();
        dimensions.forEach(value -> {
            set.add(new SavedMazePath(value, room, true));
            set.add(new SavedMazePath(value, room, false));
            return true;
        });
        return set.build();
    }

    public static Set<SavedMazePath> neighbors(MazeRoom room)
    {
        return neighbors(room, new TIntHashSet(Ranges.to(room.getDimensions())));
    }
}

