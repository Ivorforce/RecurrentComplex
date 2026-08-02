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

package ivorius.reccomplex.structures.generic.maze.components;

import com.google.common.collect.Sets;
import ivorius.ivtoolkit.tools.GuavaCollectors;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
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
            if (dist != null && strategy.connect(connection, connector, entry.getValue()))
                return shift(component, dist);
            return null;
        }).filter(Objects::nonNull);
    }

    public static <M extends MazeComponent<C>, C> ShiftedMazeComponent<M, C> shift(final M component, final MazeRoom shift)
    {
        return new ShiftedMazeComponent<>(component, shift);
    }

    public static <C> Predicate<? extends MazeComponent<C>> compatibilityPredicate(final MazeComponent<C> component, final ConnectionStrategy<C> strategy)
    {
        return input -> componentsCompatible(component, input, strategy);
    }

    public static <C> boolean componentsCompatible(final MazeComponent<C> existing, final MazeComponent<C> add, final ConnectionStrategy<C> strategy)
    {
        return !overlap(existing, add) && allExitsCompatible(existing, add, strategy);
    }

    public static boolean overlap(MazeComponent<?> left, MazeComponent<?> right)
    {
        return Sets.intersection(left.rooms(), right.rooms()).size() > 0;
    }

    public static <C> boolean allExitsCompatible(final MazeComponent<C> existing, final MazeComponent<C> add, final ConnectionStrategy<C> strategy)
    {
        return add.exits().entrySet().stream().allMatch(input -> strategy.connect(input.getKey(), existing.exits().get(input.getKey().inverse()), input.getValue()));
    }
}
