/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze;

import ivorius.ivtoolkit.maze.components.MazePassage;
import ivorius.ivtoolkit.maze.components.MazeRoom;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
     */
    public static Stream<SavedMazePath> neighborPaths(final MazeRoom room, IntStream dimensions)
    {
        return dimensions.mapToObj(d -> IntStream.of(1, -1).mapToObj(m -> new SavedMazePath(d, room, m > 0))).flatMap(t -> t);
    }

    public static Stream<SavedMazePath> neighborPaths(MazeRoom room)
    {
        return neighborPaths(room, IntStream.range(0, room.getDimensions()));
    }
}

