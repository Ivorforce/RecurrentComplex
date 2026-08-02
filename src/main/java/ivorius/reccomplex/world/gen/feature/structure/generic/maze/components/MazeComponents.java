/*
 * Copyright 2015 Lukas Tenbrink
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze.components;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import ivorius.ivtoolkit.tools.GuavaCollectors;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by lukas on 15.04.15.
 */
public class MazeComponents
{
    public static <M extends MazeComponent<C>, C> Function<M, Stream<ShiftedMazeComponent<M, C>>> shiftAllFunction(final MazePassage connection, final C connector, final ConnectionStrategy<C> strategy)
    {
        return component -> component.exits().entrySet().stream().map(entry -> {
            MazeRoom dist = entry.getKey().inverseDistance(connection);
            if (dist != null && strategy.connect(connection, connector, entry.getValue()) >= 0f)
                return shift(component, dist);
            return null;
        }).filter(Objects::nonNull);
    }

    public static <M extends MazeComponent<C>, C> ShiftedMazeComponent<M, C> shift(final M component, final MazeRoom shift)
    {
        return new ShiftedMazeComponent<>(component, shift);
    }

    public static boolean overlap(MazeComponent<?> left, MazeComponent<?> right)
    {
        return Sets.intersection(left.rooms(), right.rooms()).size() > 0;
    }

    public static <C> float connectWeight(final MazeComponent<C> existing, final MazeComponent<C> add, final ConnectionStrategy<C> strategy)
    {
        float total = 1f;
        for (Map.Entry<MazePassage, C> entry : add.exits().entrySet()) // Try
        {
            if ((total *= strategy.connect(entry.getKey(), existing.exits().get(entry.getKey().inverse()), entry.getValue())) < 0)
                return -1;
        }
        return total;
    }
}
