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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 30.09.15.
 */
public class MazePredicateMany<M extends MazeComponent<C>, C> implements MazePredicate<M, C>
{
    public final List<MazePredicate<M, C>> predicates = new ArrayList<>();

    public MazePredicateMany()
    {
    }

    public MazePredicateMany(List<MazePredicate<M, C>> predicates)
    {
        this.predicates.addAll(predicates);
    }

    @SafeVarargs
    public MazePredicateMany(MazePredicate<M, C>... predicates)
    {
        Collections.addAll(this.predicates, predicates);
    }

    @Override
    public boolean canPlace(final MorphingMazeComponent<C> maze, final ShiftedMazeComponent<M, C> component)
    {
        return predicates.stream().allMatch(p -> p.canPlace(maze, component));
    }

    @Override
    public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        predicates.forEach(p -> p.willPlace(maze, component));
    }

    @Override
    public void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        predicates.forEach(p -> p.didPlace(maze, component));
    }

    @Override
    public void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        predicates.forEach(p -> p.willUnplace(maze, component));
    }

    @Override
    public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        predicates.forEach(p -> p.didUnplace(maze, component));
    }

    @Override
    public boolean isDirtyConnection(final MazeRoom dest, final MazeRoom source, final C c)
    {
        return predicates.stream().allMatch(p -> p.isDirtyConnection(dest, source, c));
    }
}
