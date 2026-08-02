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

import com.google.common.collect.Lists;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.random.WeightedShuffler;
import ivorius.ivtoolkit.util.IvFunctions;
import ivorius.reccomplex.RecurrentComplex;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Created by lukas on 15.04.15.
 */
public class MazeComponentConnector
{
    public static int INFINITE_REVERSES = -1;

    /**
     * Its own rather than {@link RecurrentComplex#logger}, which is only assigned once the mod
     * loads - the maze search deliberately runs without any of Minecraft present.
     */
    private static final Logger logger = LogManager.getLogger(RecurrentComplex.MOD_ID);

    public static <M extends WeightedMazeComponent<C>, C> List<PlacedMazeComponent<M, C>> connect(MorphingMazeComponent<C> maze, List<M> components,
                                                                                                  ConnectionStrategy<C> connectionStrategy, final MazePredicate<C> predicate, Random random, int reverses)
    {
        // Group components that are the same together, and choose one after connecting
        List<MultiMazeComponent<M, C>> multis = IvFunctions.group(components, c -> Triple.of(c.rooms(), c.exits(), c.reachability())).stream()
                .map(c -> new MultiMazeComponent<>(Lists.newArrayList(c))).collect(Collectors.toList());

        return connectMulti(maze, multis, connectionStrategy, predicate, random, reverses).stream()
                .map(placed -> placed.component().place(placed.shift(), random))
                .collect(Collectors.toList());
    }

    protected static <M extends MultiMazeComponent<MO, C>, MO extends WeightedMazeComponent<C>, C> List<PlacedMazeComponent<M, C>> connectMulti(MorphingMazeComponent<C> maze, List<M> components,
                                                                                                                                                ConnectionStrategy<C> connectionStrategy, final MazePredicate<C> predicate, Random random, int reverses)
    {
        List<ReverseInfo<M, C>> placeOrder = new ArrayList<>();
        ReverseInfo<M, C> reversing = null;

        List<PlacedMazeComponent<M, C>> result = new ArrayList<>();
        LinkedList<Triple<MazeRoom, MazePassage, C>> exitStack = new LinkedList<>();

        Predicate<ShiftedMazeComponent<M, C>> componentPredicate = ((Predicate<ShiftedMazeComponent<M, C>>) input -> !MazeComponents.overlap(maze, input)).and(input -> predicate.canPlace(maze, input));

        addAllExits(predicate, exitStack, maze.exits().entrySet(), random);

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
                reversing.exitStack = (LinkedList<Triple<MazeRoom, MazePassage, C>>) exitStack.clone();
                reversing.shuffleSeed = random.nextLong();
            }
            else
            {
                // Reversing
                predicate.willUnplace(maze, reversing.placed);

                exitStack = (LinkedList<Triple<MazeRoom, MazePassage, C>>) reversing.exitStack.clone();
                reversing.undo.run();

                predicate.didUnplace(maze, reversing.placed);

                result.remove(result.size() - 1);
            }

            Triple<MazeRoom, MazePassage, C> triple = exitStack.removeLast();
            MazeRoom room = triple.getLeft();
            MazePassage exit = triple.getMiddle();
            C connection = triple.getRight();

            final ToDoubleFunction<ShiftedMazeComponent<M, C>> weightFunction = shifted -> shifted.getComponent().getWeight() * MazeComponents.connectWeight(maze, shifted, connectionStrategy);

            List<WeightedSelector.SimpleItem<ShiftedMazeComponent<M, C>>> shiftedComponents = components.stream()
                    .flatMap(MazeComponents.shiftAllFunction(exit, connection, connectionStrategy))
                    .map(input -> new WeightedSelector.SimpleItem<>(weightFunction.applyAsDouble(input), input))
                    .filter(w -> w.getWeight() >= 0)
                    .collect(Collectors.toCollection(ArrayList::new));

            ShiftedMazeComponent<M, C> placing = null;

            if (reversing.triedIndices > shiftedComponents.size()) // Tried more than available
                throw new RuntimeException("Maze component selection not static.");
            else if (reversing.triedIndices < shiftedComponents.size())
            {
                // More left to try
                Iterator<ShiftedMazeComponent<M, C>> shuffler = WeightedShuffler.iterateShuffled(new Random(reversing.shuffleSeed), shiftedComponents);

                for (int i = 0; i < reversing.triedIndices; i++) shuffler.next(); // Skip the tested ones
                for (ShiftedMazeComponent<M, C> comp : (Iterable<ShiftedMazeComponent<M, C>>) () -> shuffler)
                {
                    reversing.triedIndices++;
                    if (componentPredicate.test(comp))
                    {
                        placing = comp;
                        break; // Can place
                    }
                }
            }

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

            addAllExits(predicate, exitStack, placing.exits().entrySet(), random);
            reversing.undo = maze.addReversibly(placing);
            result.add(new PlacedMazeComponent<M, C>(placing.getComponent(), placing.getShift()));

            predicate.didPlace(maze, placing);

            placeOrder.add(reversing);
            reversing = null;
        }

        return result;
    }

    private static Predicate<Map.Entry<MazePassage, ?>> entryConnectsTo(final MazeRoom finalRoom)
    {
        return input -> input != null && (input.getKey().has(finalRoom));
    }

    private static <M extends WeightedMazeComponent<C>, C> void addAllExits(MazePredicate<C> placementStrategy, List<Triple<MazeRoom, MazePassage, C>> exitStack, Set<Map.Entry<MazePassage, C>> entries, Random random)
    {
        for (Map.Entry<MazePassage, C> exit : entries)
        {
            MazePassage connection = exit.getKey();
            C c = exit.getValue();

            if (placementStrategy.isDirtyConnection(connection.getLeft(), connection.getRight(), c))
                addRandomly(exitStack, random, connection, c, connection.getLeft());
            if (placementStrategy.isDirtyConnection(connection.getRight(), connection.getLeft(), c))
                addRandomly(exitStack, random, connection, c, connection.getRight());
        }
    }

    private static <C> void addRandomly(List<Triple<MazeRoom, MazePassage, C>> exitStack, Random random, MazePassage connection, C c, MazeRoom left)
    {
        exitStack.add(random.nextInt(exitStack.size() + 1), Triple.of(left, connection, c));
    }

    private static class ReverseInfo<M extends WeightedMazeComponent<C>, C>
    {
        public long shuffleSeed;
        public int triedIndices;

        /**
         * Takes {@link #placed} back out of the maze. Only valid as long as it is the most recent
         * placement still standing, which holds because placeOrder is a stack and this is replaced
         * every time the same slot is filled again. Reversing out of that order corrupts the maze
         * silently, so keep it strictly last-in-first-out.
         */
        public Runnable undo;
        public LinkedList<Triple<MazeRoom, MazePassage, C>> exitStack;
        public ShiftedMazeComponent<M, C> placed;
    }
}
