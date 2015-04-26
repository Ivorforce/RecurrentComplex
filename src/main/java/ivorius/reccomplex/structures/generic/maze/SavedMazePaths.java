/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import ivorius.ivtoolkit.maze.components.MazeRoomConnection;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * Created by lukas on 14.04.15.
 */
public class SavedMazePaths
{
    public static Function<SavedMazePath, Map.Entry<MazeRoomConnection, Connector>> toConnectionFunction(final ConnectorFactory factory)
    {
        return new Function<SavedMazePath, Map.Entry<MazeRoomConnection, Connector>>()
        {
            @Nullable
            @Override
            public Map.Entry<MazeRoomConnection, Connector> apply(@Nullable SavedMazePath input)
            {
                return input != null ? input.toRoomConnection(factory) : null;
            }
        };
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
}

