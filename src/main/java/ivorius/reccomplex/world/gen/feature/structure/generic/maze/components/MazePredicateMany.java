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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 30.09.15.
 */
public class MazePredicateMany<C> implements MazePredicate<C>
{
    public final List<MazePredicate<C>> predicates = new ArrayList<>();

    public MazePredicateMany()
    {
    }

    public MazePredicateMany(List<MazePredicate<C>> predicates)
    {
        this.predicates.addAll(predicates);
    }

    @SafeVarargs
    public MazePredicateMany(MazePredicate<C>... predicates)
    {
        Collections.addAll(this.predicates, predicates);
    }

    @Override
    public boolean canPlace(final MorphingMazeComponent<C> maze, final ShiftedMazeComponent<?, C> component)
    {
        return predicates.stream().allMatch(p -> p.canPlace(maze, component));
    }

    @Override
    public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {
        predicates.forEach(p -> p.willPlace(maze, component));
    }

    @Override
    public void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {
        predicates.forEach(p -> p.didPlace(maze, component));
    }

    @Override
    public void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {
        predicates.forEach(p -> p.willUnplace(maze, component));
    }

    @Override
    public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {
        predicates.forEach(p -> p.didUnplace(maze, component));
    }

    @Override
    public boolean isDirtyConnection(final MazeRoom dest, final MazeRoom source, final C c)
    {
        return predicates.stream().allMatch(p -> p.isDirtyConnection(dest, source, c));
    }
}
