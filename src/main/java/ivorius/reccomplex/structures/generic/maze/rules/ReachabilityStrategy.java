/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze.rules;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ivorius.ivtoolkit.maze.components.*;
import ivorius.ivtoolkit.tools.Visitor;

import java.util.*;
import java.util.function.Predicate;

/**
 * Created by lukas on 05.10.15.
 */
public class ReachabilityStrategy<M extends MazeComponent<C>, C> implements MazePredicate<M, C>
{
    private final Set<MazeRoomConnection> leftTraversed = new HashSet<>();
    private final List<Set<MazeRoomConnection>> leftNew = new ArrayList<>();

    private final Set<MazeRoomConnection> rightTraversed = new HashSet<>();
    private final List<Set<MazeRoomConnection>> rightNew = new ArrayList<>();

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

    protected static <C> Set<MazeRoomConnection> traverse(List<MazeComponent<C>> mazes, Collection<MazeRoomConnection> traversed, Set<MazeRoomConnection> connections, Predicate<C> traverser, Visitor<MazeRoomConnection> visitor)
    {
        List<MazeRoomConnection> dirty = Lists.newArrayList(connections);
        Set<MazeRoomConnection> added = new HashSet<>();

        while (!dirty.isEmpty())
        {
            MazeRoomConnection traversing = dirty.remove(0);

            for (MazeComponent<C> maze : mazes)
            {
                maze.reachability().get(traversing).forEach(dest -> {
                    if (!traversed.contains(dest) && visitor.visit(dest)
                            && traverser.test(maze.exits().get(dest)))
                    {
                        traversed.add(dest);
                        added.add(dest);
                        dirty.add(dest);
                    }
                });
            }
        }
        return added;
    }

    @Override
    public boolean canPlace(final MorphingMazeComponent<C> maze, final ShiftedMazeComponent<M, C> component)
    {
        if (stepsGoalReached >= 0)
            return true;

        // TODO Better success prediction
        final Set<MazeRoom> roomsFromBoth = Sets.union(maze.rooms(), component.rooms());
        Predicate<MazeRoomConnection> isDirty = input -> (confiner.test(input.getLeft()) && !roomsFromBoth.contains(input.getLeft())) || (confiner.test(input.getRight()) && !roomsFromBoth.contains(input.getRight()));

        place(maze, component, true);
        boolean canPlace = stepsGoalReached >= 0 || (leftTraversed.stream().anyMatch(isDirty) && rightTraversed.stream().anyMatch(isDirty));
        unplace(maze, component, true);

        return canPlace;
    }

    @Override
    public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        place(maze, component, false);
        Predicate<MazeRoomConnection> isDirty = input -> (confiner.test(input.getLeft()) && !maze.rooms().contains(input.getLeft())) || (confiner.test(input.getRight()) && !maze.rooms().contains(input.getRight()));
    }

    @Override
    public void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
    }

    @Override
    public void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {

    }

    protected void place(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component, boolean simulate)
    {
        if (stepsGoalReached >= 0)
            stepsGoalReached++;
        else
        {
            leftNew.add(traverse(maze, component, leftTraversed, rightTraversed));

            if (stepsGoalReached < 0)
                rightNew.add(traverse(maze, component, rightTraversed, leftTraversed));
        }
    }

    protected Set<MazeRoomConnection> traverse(MazeComponent<C> maze, MazeComponent<C> component, Set<MazeRoomConnection> traversed, final Collection<MazeRoomConnection> goal)
    {
        return traverse(Arrays.asList(maze, component), traversed, Sets.intersection(component.exits().keySet(), traversed), traverser, connection -> {
            if (goal.contains(connection))
            {
                stepsGoalReached = 0;
                return false;
            }

            return true;
        });
    }

    @Override
    public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        unplace(maze, component, false);
        Predicate<MazeRoomConnection> isDirty = input -> (confiner.test(input.getLeft()) && !maze.rooms().contains(input.getLeft())) || (confiner.test(input.getRight()) && !maze.rooms().contains(input.getRight()));
    }

    protected void unplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component, boolean simulate)
    {
        if (stepsGoalReached >= 0)
            stepsGoalReached--;

        if (stepsGoalReached < 0)
        {
            leftTraversed.removeAll(leftNew.remove(leftNew.size() - 1));

            if (rightNew.size() > leftNew.size())
                rightTraversed.removeAll(rightNew.remove(rightNew.size() - 1));
        }
    }

    @Override
    public boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c)
    {
        return true;
    }
}
