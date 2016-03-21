/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TLinkedHashSet;
import ivorius.ivtoolkit.maze.components.*;
import ivorius.ivtoolkit.tools.Visitor;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by lukas on 05.10.15.
 */
public class ReachabilityStrategy<M extends MazeComponent<C>, C> implements MazePredicate<M, C>
{
    private final LinkedHashSet<MazeRoomConnection> leftTraversed = new LinkedHashSet<>();
    private final TIntList leftTraversedSteps = new TIntArrayList();

    private final LinkedHashSet<MazeRoomConnection> rightTraversed = new LinkedHashSet<>();
    private final TIntList rightTraversedSteps = new TIntArrayList();

    private final Predicate<MazeRoom> confiner;
    private final Predicate<C> traverser;

    private int stepsGoalReached = -1;

    public ReachabilityStrategy(Set<MazeRoomConnection> start, Set<MazeRoomConnection> end, Predicate<C> traverser, Predicate<MazeRoom> confiner)
    {
        leftTraversed.addAll(start);
        rightTraversed.addAll(end);
        this.traverser = traverser;
        this.confiner = confiner;
    }

    public static <C> Predicate<C> connectorTraverser(final Set<C> blockingConnections)
    {
        return input -> !blockingConnections.contains(input);
    }

    // TODO Better success prediction
    protected static <C> void traverse(List<MazeComponent<C>> mazes, Collection<MazeRoomConnection> traversed, Set<MazeRoomConnection> connections, Predicate<C> traverser, Visitor<MazeRoomConnection> visitor)
    {
        List<MazeRoomConnection> dirty = Lists.newArrayList(connections);

        while (!dirty.isEmpty())
        {
            MazeRoomConnection traversing = dirty.remove(0);

            for (MazeComponent<C> maze : mazes)
            {
                maze.reachability().stream().filter(pair -> pair.getLeft().equals(traversing)).forEach(pair -> {
                    MazeRoomConnection dest = pair.getRight();

                    if (!traversed.contains(dest) && visitor.visit(dest)
                            && traverser.apply(maze.exits().get(traversing)) && traverser.apply(maze.exits().get(dest)))
                    {
                        traversed.add(dest);
                        dirty.add(dest);
                    }
                });
            }
        }
    }

    @Override
    public boolean canPlace(final MorphingMazeComponent<C> maze, final ShiftedMazeComponent<M, C> component)
    {
        if (stepsGoalReached >= 0)
            return true;

        final Set<MazeRoom> roomsFromBoth = Sets.union(maze.rooms(), component.rooms());
        Predicate<MazeRoomConnection> isExit = input -> (confiner.apply(input.getLeft()) && !roomsFromBoth.contains(input.getLeft())) || (confiner.apply(input.getRight()) && !roomsFromBoth.contains(input.getRight()));

        willPlace(maze, component); // Simulate
        boolean canPlace = leftTraversed.stream().anyMatch(isExit::apply) && rightTraversed.stream().anyMatch(isExit::apply);
        didUnplace(maze, component);

        return canPlace;
    }

    @Override
    public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        if (stepsGoalReached < 0)
            traverse(maze, component, leftTraversed, leftTraversedSteps, rightTraversed);
        if (stepsGoalReached < 0)
            traverse(maze, component, rightTraversed, rightTraversedSteps, leftTraversed);

        if (stepsGoalReached >= 0)
            stepsGoalReached ++;
    }

    protected void traverse(MazeComponent<C> maze, MazeComponent<C> component, LinkedHashSet<MazeRoomConnection> leftTraversed, TIntList leftTraversedSteps, final Collection<MazeRoomConnection> right)
    {
        int before = leftTraversed.size();
        traverse(Arrays.asList(maze, component), leftTraversed, Sets.intersection(component.exits().keySet(), leftTraversed), traverser, connection -> {
            if (right.contains(connection))
            {
                stepsGoalReached = 0;
                return false;
            }

            return true;
        });
        leftTraversedSteps.add(leftTraversed.size() - before);
    }

    @Override
    public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        if (stepsGoalReached >= 0)
            stepsGoalReached--;

        if (stepsGoalReached < 0)
        {
            removeLast(leftTraversed, leftTraversedSteps.removeAt(leftTraversedSteps.size() - 1));

            while (rightTraversed.size() > leftTraversed.size())
                removeLast(rightTraversed, rightTraversedSteps.removeAt(rightTraversedSteps.size() - 1));
        }
    }

    // TODO Better approach
    private void removeLast(Collection<?> collection, int objects)
    {
        if (objects < 1)
            return;

        int i = collection.size() - objects;
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext(); )
        {
            iterator.next();
            if (--i < 0)
                iterator.remove();
        }
    }

    @Override
    public boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c)
    {
        return true;
    }
}
