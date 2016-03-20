/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.common.base.Predicate;
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
    private final List<Set<MazeRoomConnection>> left = new ArrayList<>();
    private final LinkedHashSet<MazeRoomConnection> leftTraversed = new LinkedHashSet<>();
    private final TIntList leftTraversedSteps = new TIntArrayList();

    private final List<Set<MazeRoomConnection>> right = new ArrayList<>();
    private final LinkedHashSet<MazeRoomConnection> rightTraversed = new LinkedHashSet<>();
    private final TIntList rightTraversedSteps = new TIntArrayList();

    private final Predicate<C> traverser;

    private int stepsGoalReached = -1;

    public ReachabilityStrategy(Set<MazeRoomConnection> start, Set<MazeRoomConnection> end, Predicate<C> traverser)
    {
        left.add(Sets.newHashSet(start));
        leftTraversed.addAll(start);
        right.add(Sets.newHashSet(end));
        rightTraversed.addAll(end);
        this.traverser = traverser;
    }

    public static <C> Predicate<C> connectorTraverser(final Set<C> blockingConnections)
    {
        return new Predicate<C>()
        {
            @Override
            public boolean apply(@Nullable C input)
            {
                return !blockingConnections.contains(input);
            }
        };
    }

    protected static <C> void traverse(List<MazeComponent<C>> mazes, Collection<MazeRoomConnection> traversed, Set<MazeRoomConnection> connections, Predicate<C> traverser, Visitor<MazeRoomConnection> visitor)
    {
        List<MazeRoomConnection> dirty = Lists.newArrayList(connections);
        connections.clear();

        while (!dirty.isEmpty())
        {
            MazeRoomConnection traversing = dirty.remove(0);

            for (MazeComponent<C> maze : mazes)
            {
                for (Pair<MazeRoomConnection, MazeRoomConnection> pair : maze.reachability())
                {
                    if (pair.getLeft().equals(traversing) && !traversed.contains(pair.getRight()) && visitor.visit(pair.getRight())
                            && traverser.apply(maze.exits().get(traversing)) && traverser.apply(maze.exits().get(pair.getRight())))
                    {
                        traversed.add(pair.getRight());
                        dirty.add(pair.getRight());
                    }
                }
            }
        }
    }

    @Override
    public boolean canPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        if (stepsGoalReached >= 0)
            return true;

        willPlace(maze, component); // Simulate
        boolean canContinue = left.get(left.size() - 1).size() > 0 && right.get(right.size() - 1).size() > 0;
        didUnplace(maze, component);

        return canContinue;
    }

    @Override
    public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        if (stepsGoalReached < 0)
            left.add(traverse(maze, component, left.get(left.size() - 1), leftTraversed, leftTraversedSteps, right.get(right.size() - 1)));
        if (stepsGoalReached < 0)
            right.add(traverse(maze, component, right.get(right.size() - 1), rightTraversed, rightTraversedSteps, left.get(left.size() - 1)));
    }

    protected Set<MazeRoomConnection> traverse(MazeComponent<C> maze, MazeComponent<C> component, Collection<MazeRoomConnection> left, LinkedHashSet<MazeRoomConnection> leftTraversed, TIntList leftTraversedSteps, final Collection<MazeRoomConnection> right)
    {
        Set<MazeRoomConnection> dirty = Sets.newHashSet(left);
        int before = leftTraversed.size();
        traverse(Arrays.asList(maze, component), leftTraversed, dirty, traverser, new Visitor<MazeRoomConnection>()
        {
            @Override
            public boolean visit(MazeRoomConnection connection)
            {
                if (right.contains(connection))
                {
                    stepsGoalReached = 0;
                    return false;
                }

                return true;
            }
        });
        leftTraversedSteps.add(leftTraversed.size() - before);
        return dirty;
    }

    @Override
    public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        if (stepsGoalReached >= 0)
            stepsGoalReached--;

        if (stepsGoalReached < 0)
        {
            left.remove(left.size() - 1);
            removeLast(leftTraversed, leftTraversedSteps.removeAt(leftTraversedSteps.size() - 1));

            while (right.size() > left.size())
            {
                right.remove(right.size() - 1);
                removeLast(rightTraversed, rightTraversedSteps.removeAt(rightTraversedSteps.size() - 1));
            }
        }
    }

    // Don't look
    // TODO Better approach
    private void removeLast(Collection<?> collection, int objects)
    {
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
