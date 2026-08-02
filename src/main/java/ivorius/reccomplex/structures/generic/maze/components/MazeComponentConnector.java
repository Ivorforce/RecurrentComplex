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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import ivorius.ivtoolkit.random.WeightedShuffler;
import ivorius.reccomplex.RecurrentComplex;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by lukas on 15.04.15.
 */
public class MazeComponentConnector
{
    public static int INFINITE_REVERSES = -1;

    /**
     * Its own rather than {@link RecurrentComplex#logger}, which is only assigned once the mod
     * loads - the maze search does not otherwise need any of Minecraft present.
     */
    private static final Logger logger = LogManager.getLogger(RecurrentComplex.MODID);

    @Deprecated
    public static <M extends WeightedMazeComponent<C>, C> List<ShiftedMazeComponent<M, C>> randomlyConnect(MorphingMazeComponent<C> morphingComponent, List<M> components,
                                                                                                           ConnectionStrategy<C> connectionStrategy, final MazeComponentPlacementStrategy<M, C> placementStrategy, Random random)
    {
        return randomlyConnect(morphingComponent, components, connectionStrategy, new MazePredicate<M, C>()
        {
            @Override
            public boolean canPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
            {
                return placementStrategy.canPlace(component);
            }

            @Override
            public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
            {

            }

            @Override
            public void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
            {

            }

            @Override
            public void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
            {

            }

            @Override
            public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
            {

            }

            @Override
            public boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c)
            {
                return placementStrategy.shouldContinue(dest, source, c);
            }
        }, random, 0);
    }

    public static <M extends WeightedMazeComponent<C>, C> List<ShiftedMazeComponent<M, C>> randomlyConnect(MorphingMazeComponent<C> maze, List<M> components,
                                                                                                           ConnectionStrategy<C> connectionStrategy, final MazePredicate<M, C> predicate, Random random, int reverses)
    {
        List<ReverseInfo<M, C>> placeOrder = new ArrayList<>();
        ReverseInfo<M, C> reversing = null;

        List<ShiftedMazeComponent<M, C>> result = new ArrayList<>();
        ArrayDeque<Triple<MazeRoom, MazePassage, C>> exitStack = new ArrayDeque<>();

        Predicate<ShiftedMazeComponent<M, C>> componentPredicate = ((Predicate<ShiftedMazeComponent<M, C>>) MazeComponents.compatibilityPredicate(maze, connectionStrategy)).and(input -> predicate.canPlace(maze, input));

        addAllExits(predicate, exitStack, maze.exits().entrySet());

        while (exitStack.size() > 0)
        {
            if (reversing == null)
            {
                if (maze.rooms().contains(exitStack.peekLast().getLeft()))
                {
                    exitStack.removeLast(); // Skip: Has been filled while queued
                    continue;
                }

                // Backing Up
                reversing = new ReverseInfo<>();
                reversing.exitStack = exitStack.clone();
                reversing.maze = maze.copy();
                reversing.shuffleSeed = random.nextLong();
            }
            else
            {
                // Reversing
                predicate.willUnplace(maze, reversing.placed);

                exitStack = reversing.exitStack.clone(); // TODO Do a more efficient DIFF approach
                maze.set(reversing.maze); // TODO Do a more efficient DIFF approach

                predicate.didUnplace(maze, reversing.placed);

                result.remove(result.size() - 1);
            }

            Triple<MazeRoom, MazePassage, C> triple = exitStack.removeLast();
            MazeRoom room = triple.getLeft();
            MazePassage exit = triple.getMiddle();
            C connection = triple.getRight();

            List<ShiftedMazeComponent<M, C>> shuffled = Lists.newArrayList(
                    components.stream().flatMap(MazeComponents.shiftAllFunction(exit, connection, connectionStrategy))
                            .collect(Collectors.toList())
            );
            WeightedShuffler.shuffle(new Random(reversing.shuffleSeed), shuffled, shifted -> shifted.getComponent().getWeight());

            if (reversing.triedIndices > shuffled.size())
                throw new RuntimeException("Maze component selection not static.");

            ShiftedMazeComponent<M, C> placing = null;
            while ((placing == null || !componentPredicate.test(placing)) && reversing.triedIndices < shuffled.size())
                placing = shuffled.get(reversing.triedIndices++);
            if (reversing.triedIndices >= shuffled.size()) placing = null;

            if (placing == null)
            {
                if (reverses == 0)
                {
                    logger.warn("Did not find fitting component for maze!");
                    logger.warn("Suggested: X with exits " + maze.exits().entrySet().stream().filter(entryConnectsTo(room)).collect(Collectors.toList()));

                    reversing = null;
                }
                else
                {
                    if (reverses > 0) reverses--;

                    if (placeOrder.size() == 0)
                    {
                        logger.warn("Maze is not completable!");
                        logger.warn("Switching to flawed mode.");
                        reverses = 0;
                        reversing = null;
                    }
                    else
                        reversing = placeOrder.remove(placeOrder.size() - 1);
                }

                continue;
            }

            reversing.placed = placing;

            // Placing
            predicate.willPlace(maze, placing);

            addAllExits(predicate, exitStack, placing.exits().entrySet());
            maze.add(placing);
            result.add(placing);

            predicate.didPlace(maze, placing);

            placeOrder.add(reversing);
            reversing = null;
        }

        return ImmutableList.<ShiftedMazeComponent<M, C>>builder().addAll(result).build();
    }

    private static Predicate<Map.Entry<MazePassage, ?>> entryConnectsTo(final MazeRoom finalRoom)
    {
        return input -> input != null && (input.getKey().has(finalRoom));
    }

    private static <M extends WeightedMazeComponent<C>, C> void addAllExits(MazePredicate<M, C> placementStrategy, Deque<Triple<MazeRoom, MazePassage, C>> exitStack, Set<Map.Entry<MazePassage, C>> entries)
    {
        for (Map.Entry<MazePassage, C> exit : entries)
        {
            MazePassage connection = exit.getKey();
            C c = exit.getValue();

            if (placementStrategy.isDirtyConnection(connection.getLeft(), connection.getRight(), c))
                exitStack.add(Triple.of(connection.getLeft(), connection, c));
            if (placementStrategy.isDirtyConnection(connection.getRight(), connection.getLeft(), c))
                exitStack.add(Triple.of(connection.getRight(), connection, c));
        }
    }

    private static class ReverseInfo<M extends WeightedMazeComponent<C>, C>
    {
        public long shuffleSeed;
        public int triedIndices;

        public MorphingMazeComponent<C> maze;
        public ArrayDeque<Triple<MazeRoom, MazePassage, C>> exitStack;
        public ShiftedMazeComponent<M, C> placed;
    }
}
