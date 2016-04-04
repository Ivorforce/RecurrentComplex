/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze.rules;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ivorius.ivtoolkit.maze.components.*;
import ivorius.ivtoolkit.tools.Visitor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by lukas on 05.10.15.
 */
public class ReachabilityStrategy<M extends MazeComponent<C>, C> implements MazePredicate<M, C>
{
    private final Set<Pair<MazeRoom, Set<MazeRoom>>> traversalAbilities = new HashSet<>();

    private final Set<MazePassage> leftTraversed = new HashSet<>();
    private final List<Set<MazePassage>> leftNew = new ArrayList<>();

    private final Set<MazePassage> rightTraversed = new HashSet<>();
    private final List<Set<MazePassage>> rightNew = new ArrayList<>();

    private final Predicate<MazeRoom> confiner;
    private final Predicate<C> traverser;

    private int stepsGoalReached = -1;

    public ReachabilityStrategy(Set<MazePassage> start, Set<MazePassage> end, Predicate<C> traverser, Predicate<MazeRoom> confiner, Set<Pair<MazeRoom, Set<MazeRoom>>> traversalAbilities)
    {
        leftTraversed.addAll(start);
        leftTraversed.addAll(start.stream().map(MazePassage::inverse).collect(Collectors.toList())); // It's an exit so we're on both sides
        rightTraversed.addAll(end);
        rightTraversed.addAll(end.stream().map(MazePassage::inverse).collect(Collectors.toList()));
        this.traverser = traverser;
        this.confiner = confiner;
        this.traversalAbilities.addAll(traversalAbilities);
    }

    public static <C> Set<Pair<MazeRoom, Set<MazeRoom>>> compileAbilities(Collection<? extends MazeComponent<C>> components, Predicate<C> traverser)
    {
        Set<Pair<MazeRoom, Set<MazeRoom>>> abilities = new HashSet<>();

        for (MazeComponent<C> component : components)
        {
            // Exits leading outside
            component.exits().forEach((passage, c) -> {
                if (traverser.test(c))
                    abilities.add(Pair.of(passage.normalize().getDest(), Collections.emptySet()));
            });

            // Walking within the component
            for (Map.Entry<MazePassage, MazePassage> entry : component.reachability().entries())
            {
                if (!traverser.test(component.exits().get(entry.getValue())))
                    continue;

                MazePassage passage = new MazePassage(entry.getKey().getSource(), entry.getValue().getSource());
                abilities.add(Pair.of(passage.normalize().getDest(), component.rooms().stream().map(r -> r.sub(passage.getSource())).collect(Collectors.toSet())));
            }
        }

        // Remove inferrable abilities
        for (Iterator<Pair<MazeRoom, Set<MazeRoom>>> iterator = abilities.iterator(); iterator.hasNext(); )
        {
            Pair<MazeRoom, Set<MazeRoom>> ability = iterator.next();
            MazeRoom nullRoom = new MazeRoom(new int[ability.getLeft().getDimensions()]);

            if (canReach(ability.getRight(), abilities.stream().filter(a -> !a.equals(ability)).collect(Collectors.toSet()), Collections.singleton(nullRoom), Collections.singleton(ability.getLeft()), null))
                iterator.remove();
        }

        return abilities;
    }

    public static <C> Predicate<C> connectorTraverser(final Set<C> blockingConnections)
    {
        return input -> !blockingConnections.contains(input);
    }

    protected static <C> Set<MazePassage> traverse(Collection<MazeComponent<C>> mazes, Collection<MazePassage> traversed, Set<MazePassage> connections, Predicate<C> traverser, Visitor<MazePassage> visitor)
    {
        List<MazePassage> dirty = Lists.newArrayList(connections);
        Set<MazePassage> added = new HashSet<>();

        while (!dirty.isEmpty())
        {
            MazePassage traversing = dirty.remove(0);

            for (MazeComponent<C> maze : mazes)
            {
                maze.reachability().get(traversing).forEach(dest -> {
                    if (!traversed.contains(dest) && visitor.visit(dest))
                    {
                        if (traverser.test(maze.exits().get(dest)))
                        {
                            MazePassage rDest = dest.inverse(); // We are now on the other side of the connection/'wall'

                            traversed.add(rDest);
                            dirty.add(rDest);
                            added.add(rDest);
                        }

                        traversed.add(dest);
                        dirty.add(dest);
                        added.add(dest);
                    }
                });
            }
        }
        return added;
    }

    private static boolean canReach(Set<MazeRoom> rooms, Set<Pair<MazeRoom, Set<MazeRoom>>> abilities, Set<MazeRoom> left, Set<MazeRoom> right, Predicate<MazeRoom> confiner)
    {
        if (left.size() <= 0 || right.size() <= 0)
            return false;

        Predicate<MazeRoom> predicate = confiner != null ? confiner.and((o) -> !rooms.contains(o)) : rooms::contains;

        Set<MazeRoom> visited = Sets.newHashSet(left);
        TreeSet<MazeRoom> dirty = Sets.newTreeSet((o1, o2) -> {
            int compare = Double.compare(minDistanceSQ(o1, right), minDistanceSQ(o2, right));
            return compare != 0 ? compare : compare(o1.getCoordinates(), o2.getCoordinates());
        });
        dirty.addAll(left);
        visited.addAll(left);

        while (!dirty.isEmpty())
        {
            MazeRoom cur = dirty.pollFirst();
            for (MazeRoom next : (Iterable<MazeRoom>) abilities.stream().filter(e -> e.getValue().stream().map(p -> p.add(cur)).allMatch(predicate)).map(p -> p.getKey().add(cur))::iterator)
            {
                if (right.contains(next))
                    return true;

                if (predicate.test(next) && visited.add(next))
                    dirty.add(next);
            }
        }

        return false;
    }

    private static int compare(int[] left, int[] right)
    {
        for (int i = 0; i < left.length; i++)
        {
            int cmp = Integer.compare(left[i], right[i]);
            if (cmp != 0)
                return cmp;
        }

        return 0;
    }

    private static double minDistanceSQ(MazeRoom room, Collection<MazeRoom> rooms)
    {
        return rooms.stream().mapToDouble(r -> room.distance(room)).min().orElse(0);
    }

    @Override
    public boolean canPlace(final MorphingMazeComponent<C> maze, final ShiftedMazeComponent<M, C> component)
    {
        if (stepsGoalReached >= 0)
            return true;

        final Set<MazeRoom> roomsFromBoth = Sets.union(maze.rooms(), component.rooms());
        Predicate<MazePassage> isDirty = input -> confiner.test(input.getSource()) && !roomsFromBoth.contains(input.getSource());

        place(maze, component, true);
        boolean canPlace = stepsGoalReached >= 0 || canReach(roomsFromBoth, traversalAbilities, (leftTraversed.stream().filter(isDirty).map(MazePassage::getDest).collect(Collectors.toSet())), rightTraversed.stream().filter(isDirty).map(MazePassage::getDest).collect(Collectors.toSet()), confiner);
        unplace(maze, component, true);

        return canPlace;
    }

    @Override
    public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        place(maze, component, false);
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

    protected Set<MazePassage> traverse(MazeComponent<C> maze, MazeComponent<C> component, Set<MazePassage> traversed, final Collection<MazePassage> goal)
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
