/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules;

import com.google.common.collect.Sets;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import ivorius.ivtoolkit.maze.components.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by lukas on 05.10.15.
 */
public class ReachabilityStrategy<C> implements MazePredicate<C>
{
    private final Collection<Ability<C>> traversalAbilities = new ArrayList<>();

    private ConnectionPoint mainConnectionPoint;
    private final List<ConnectionPoint> connectionPoints = new ArrayList<>();
    private final TObjectIntMap<ConnectionPoint> stepsReached = new TObjectIntHashMap<>();

    private final Predicate<MazeRoom> confiner;
    private final ConnectionStrategy<C> connectionStrategy;

    private boolean preventConnection;

    public ReachabilityStrategy(Predicate<MazeRoom> confiner, ConnectionStrategy<C> connectionStrategy, boolean preventConnection)
    {
        this.confiner = confiner;
        this.connectionStrategy = connectionStrategy;
        this.preventConnection = preventConnection;
    }

    public static <C> ReachabilityStrategy<C> connect(Collection<Collection<MazePassage>> points, Predicate<MazeRoom> confiner, Collection<Ability<C>> traversalAbilities, ConnectionStrategy<C> connectionStrategy)
    {
        ReachabilityStrategy<C> strategy = new ReachabilityStrategy<>(confiner, connectionStrategy, false);
        strategy.setConnection(points);
        strategy.traversalAbilities.addAll(traversalAbilities);
        return strategy;
    }

    public static <C> ReachabilityStrategy<C> preventConnection(Collection<Collection<MazePassage>> points, Predicate<MazeRoom> confiner, ConnectionStrategy<C> connectionStrategy)
    {
        ReachabilityStrategy<C> strategy = new ReachabilityStrategy<>(confiner, connectionStrategy, true);
        strategy.setConnection(points);
        return strategy;
    }

    public static <C> Collection<Ability<C>> compileAbilities(Collection<? extends MazeComponent<C>> components)
    {
        Collection<Ability<C>> abilities = new HashSet<>();

        for (MazeComponent<C> component : components)
        {
            // Walking within the component, and at last outside
            for (MazePassage source : component.reachability().keySet())
            {
                // Can only start walking if it starts within the component
                if (component.rooms().contains(source.getSource()))
                {
                    // TODO Don't use a traversed Set since we don't need it
                    for (MazePassage exit : traverse(Collections.singleton(component), new HashSet<>(), Collections.singleton(source), null))
                    {
                        // Only if we can exit the component here it's a true ability
                        if (!component.rooms().contains(exit.getSource())
                                // If it's just the same exit flipped it's not a walk through the component... This helps a bit with prediction
                                && !source.equals(exit.inverse()))
                        {
                            abilities.add(Ability.from(source, exit, component));
                        }
                    }
                }
            }
        }

//         An ability starts where you can place a room, and stops where you can place the next room
        // Remove inferrable abilities
        for (Iterator<Ability<C>> iterator = abilities.iterator(); iterator.hasNext(); )
        {
            Ability<C> ability = iterator.next();

            if (approximateCanReach(ability.rooms, (c, p) -> compatible(ability.exits.get(p), c),
                    abilities.stream().filter(a -> !a.equals(ability)).collect(Collectors.toSet()),
                    Collections.singleton(ability.start),
                    Collections.singleton(ability.destination())
                    , null))
                iterator.remove();
        }

        return abilities;
    }

    protected static <C> boolean compatible(C existing, C add)
    {
        return existing == null || add == null || existing.equals(add);
    }

    public static <C> Predicate<C> connectorTraverser(final Set<C> blockingConnections)
    {
        return input -> !blockingConnections.contains(input);
    }

    protected static <C> Set<MazePassage> traverse(Collection<MazeComponent<C>> mazes, @Nonnull Collection<MazePassage> traversed, Collection<MazePassage> connections, @Nullable Consumer<MazePassage> visitor)
    {
        if (connections.size() <= 0)
            return Collections.emptySet();

        Deque<MazePassage> dirty = new ArrayDeque<>(connections);
        Set<MazePassage> added = new HashSet<>();

        MazePassage src;
        while ((src = dirty.pollFirst()) != null)
        {
            for (MazeComponent<C> maze : mazes)
            {
                maze.reachability().get(src).forEach(dest ->
                {
                    // Have we been here already?
                    if (traversed.add(dest))
                    {
                        if (visitor != null) visitor.accept(dest);
                        added.add(dest);
                        dirty.addLast(dest);

                        // Don't need to do this - the reachability already contains a 'going outside' path
//                        // Try to go through path
//                        MazePassage rDest = dest.inverse();
//                        if (traverser.test(maze.exits().get(dest)) && traversed.add(rDest))
//                        {
//                            // We are now on the other side of the connection/'wall'
//                            if (visitor != null) visitor.accept(rDest);
//                            added.add(rDest);
//                            dirty.addLast(rDest);
//                        }
                    }
                });
            }
        }
        return added;
    }

    private static <C> boolean approximateCanReach(Set<MazeRoom> rooms, BiPredicate<C, MazePassage> connector, Collection<Ability<C>> abilities, Set<MazePassage> left, Set<MazePassage> right, Predicate<MazeRoom> confiner)
    {
        return approximateCanReach(rooms, abilities, Collections.emptyList(), left, right, Collections.emptyList(), confiner, connector);
    }

    private static <C> boolean approximateCanReach(Set<MazeRoom> rooms, Collection<Ability<C>> abilities, Collection<MazeComponent<C>> mazes, Set<MazePassage> left, Set<MazePassage> right, Collection<MazePassage> pTraversed, Predicate<MazeRoom> confiner, BiPredicate<C, MazePassage> connector)
    {
        if (left.size() <= 0 || right.size() <= 0)
            return false;

        // This actually might happen
        if (left.stream().anyMatch(right::contains))
            return true;

        final Collection<MazePassage> traversed = Sets.newHashSet(pTraversed); // Editable

        Predicate<MazeRoom> roomPlaceable = confiner != null ? ((o) -> confiner.test(o) && !rooms.contains(o)) : rooms::contains;
        Predicate<MazePassage> passagePlaceable = o -> roomPlaceable.test(o.getSource());

        Set<MazePassage> visited = Sets.newHashSet(left);
        TreeSet<MazePassage> dirty = Sets.newTreeSet((o1, o2) ->
        {
            int compare;
            // Sort by closest
            if ((compare = Double.compare(minDistanceSQ(o1, right), minDistanceSQ(o2, right))) != 0) return compare;

            // Arbitrarily sort - different passages can NEVER return 0, otherwise one gets trashed
            if ((compare = compare(o1.getSource().getCoordinates(), o2.getSource().getCoordinates())) != 0)
                return compare;
            if ((compare = compare(o1.getDest().getCoordinates(), o2.getDest().getCoordinates())) != 0) return compare;

            return 0;
        });
        dirty.addAll(left);
        visited.addAll(left);

        MazePassage curPre;
        while ((curPre = dirty.pollFirst()) != null)
        {
            MazePassage cur = curPre;
            MazePassage curNormal = cur.normalize();

            // Try each ability (i.e. walk through empty space)
            for (Ability ability : (Iterable<Ability<C>>) abilities.stream()
                    .filter(ability -> !visited.contains(ability.destination().add(cur.getSource()))) // Wasn't there
                    .filter(ability -> ability.start.getDest().equals(curNormal.getDest())) // Shiftable
                    .filter(ability -> ability.rooms.stream().map(r -> r.add(cur.getSource())).allMatch(roomPlaceable)) // Have room
                    .filter(ability -> ability.exits.keySet().stream() // Connectable
                            .allMatch(p -> connector.test(ability.exits.get(p), p.add(cur.getSource())))
                    )
                    ::iterator)
            {
                MazePassage dest = ability.destination().add(cur.getSource());
                if (right.contains(dest))
                    return true;
                if (passagePlaceable.test(dest) && visited.add(dest))
                    dirty.add(dest);

                // Try entries (i.e. walk through placed components)
                for (MazePassage p : (Iterable<MazePassage>)
                        traverse(mazes, traversed, Collections.singleton(dest), null).stream()
                                .distinct()::iterator)
                {
                    if (right.contains(p))
                        return true;
                    if (passagePlaceable.test(p) && visited.add(p))
                        dirty.add(p);
                }
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

    private static double minDistanceSQ(MazePassage passage, Collection<MazePassage> rooms)
    {
        return rooms.stream().map(MazePassage::getDest).mapToDouble(o -> o.distanceSQ(passage.getSource())).min().orElseThrow(InternalError::new);
    }

    protected static <C> C exitFromEither(MazeComponent<C> left, MazeComponent<C> right, MazePassage p)
    {
        C c = left.exits().get(p);
        return c != null ? c : right.exits().get(p);
    }

    protected void setConnection(Collection<Collection<MazePassage>> points)
    {
        connectionPoints.addAll(points.stream().map(p -> new ConnectionPoint(p, p.stream().map(MazePassage::inverse).collect(Collectors.toList()))).collect(Collectors.toList()));

        mainConnectionPoint = connectionPoints.size() > 0 ? connectionPoints.remove(0) : null;
    }

    @Override
    public boolean canPlace(final MorphingMazeComponent<C> maze, final ShiftedMazeComponent<?, C> component)
    {
        if (preventConnection && !stepsReached.isEmpty())
            return true; // Already Connected: Give Up

        if (stepsReached.size() == connectionPoints.size())
            return true; // Done

        Predicate<MazePassage> isDirtyPre = dirtyPassages(maze.exits().keySet());

        boolean[] unconnectable = new boolean[connectionPoints.size()];
        for (int i = 0; i < connectionPoints.size(); i++)
        {
            ConnectionPoint point = connectionPoints.get(i);
            if (point.traversed.stream().noneMatch(isDirtyPre))
                unconnectable[i] = true; // Has no more openings! It's either reached or given up.
        }

        place(maze, component, true);

        final Set<MazeRoom> roomsFromBoth = Sets.union(maze.rooms(), component.rooms());
        final Set<MazePassage> exitsFromBoth = Sets.union(maze.exits().keySet(), component.exits().keySet());
        Predicate<MazePassage> isDirty = dirtyPassages(exitsFromBoth);

        boolean canPlace;
        if (preventConnection)
            canPlace = stepsReached.isEmpty();
        else
        {
            canPlace = true;
            for (int i = 0; i < connectionPoints.size(); i++)
            {
                ConnectionPoint point = connectionPoints.get(i);

                canPlace = stepsReached.containsKey(point) || unconnectable[i] || approximateCanReach(
                        roomsFromBoth,
                        traversalAbilities,
                        Arrays.asList(maze, component),
                        // Use getSource here since we need to have been on the other side if we want to connect
                        point.traversed.stream().filter(isDirty).collect(Collectors.toSet()),
                        mainConnectionPoint.traversed.stream().filter(isDirty).map(MazePassage::inverse).collect(Collectors.toSet()),
                        point.traversed,
                        confiner,
                        (c, p) -> connectionStrategy.connect(p, exitFromEither(maze, component, p.inverse()), c) > 0);
                if (!canPlace) // Can skip checking the rest
                    break;
            }
        }

        unplace(maze, component, true);

        return canPlace;
    }

    @Nonnull
    protected Predicate<MazePassage> dirtyPassages(Set<MazePassage> r)
    {
        // Source because the dirty passages always point inside (since we're outside)
        return input -> confiner.test(input.getSource()) && !r.contains(input);
    }

    @Override
    public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {
        place(maze, component, false);
    }

    @Override
    public void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {
    }

    @Override
    public void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {

    }

    protected void place(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component, boolean simulate)
    {
        if (stepsReached.size() == connectionPoints.size())
            stepsReached.transformValues(i -> i + 1);
        else
        {
            for (ConnectionPoint point : connectionPoints)
            {
                if (stepsReached.containsKey(point))
                    stepsReached.adjustValue(point, 1);
                else
                    point.order.add(traverse(maze, component, point.traversed, mainConnectionPoint.traversed, p -> stepsReached.put(point, 0)));
            }

            mainConnectionPoint.order.add(traverse(maze,
                    component,
                    mainConnectionPoint.traversed,
                    connectionPoints.stream().filter(point -> !stepsReached.containsKey(point)).flatMap(point -> point.traversed.stream()).collect(Collectors.toList()),
                    p -> connectionPoints.stream().filter(point -> point.traversed.contains(p)).forEach(point -> stepsReached.put(point, 0))));
        }
    }

    protected Set<MazePassage> traverse(MazeComponent<C> maze, MazeComponent<C> component, Set<MazePassage> traversed, final Collection<MazePassage> goal, Consumer<MazePassage> goalConsumer)
    {
        return traverse(Arrays.asList(maze, component), traversed, Sets.intersection(component.exits().keySet(), traversed), connection ->
        {
            if (goal.contains(connection))
                goalConsumer.accept(connection);
        });
    }

    @Override
    public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {
        unplace(maze, component, false);
    }

    protected void unplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component, boolean simulate)
    {
        stepsReached.transformValues(i -> i - 1);
        stepsReached.retainEntries((a, i) -> i >= 0);

        if (stepsReached.size() < connectionPoints.size())
        {
            mainConnectionPoint.reverseStep();

            connectionPoints.stream().filter(point -> point.order.size() > mainConnectionPoint.order.size()).forEach(ConnectionPoint::reverseStep);
        }
    }

    @Override
    public boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c)
    {
        return true;
    }

    protected Function<MazeRoom, String> dirtyMarker(MazeComponent component, @Nullable MazeComponent place)
    {
        return r ->
        {
            if (isDirty(r, mainConnectionPoint, component))
                return "0";
            else
            {
                int p = connectionPoints.stream().filter(point -> isDirty(r, point, component))
                        .mapToInt(connectionPoints::indexOf).findFirst().orElse(-1);

                if (p >= 0)
                    return "" + (p + 1);
            }

            if (place != null && place.rooms().contains(r))
                return "O";

            return null;
        };
    }

    private boolean isDirty(MazeRoom r, ConnectionPoint point, MazeComponent<?> component)
    {
        return !stepsReached.containsKey(point) && point.traversed.stream()
                .filter(dirtyPassages(component.exits().keySet()))
                .anyMatch(r::equals);
    }

    private static class Ability<C>
    {
        @Nonnull
        public final MazePassage start;
        @Nonnull
        public final MazePassage destination;
        @Nonnull
        public final Set<MazeRoom> rooms;
        @Nonnull
        public final Map<MazePassage, C> exits;

        public Ability(@Nonnull MazePassage start, @Nonnull MazePassage destination, @Nonnull Set<MazeRoom> rooms, @Nonnull Map<MazePassage, C> exits)
        {
            this.start = start;
            this.destination = destination;
            this.rooms = rooms;
            this.exits = exits;
        }

        public static <C> Ability<C> from(@Nonnull MazePassage start, @Nonnull MazePassage destination, MazeComponent<C> component)
        {
            return new Ability<>(start.normalize(), destination.sub(start.getSource()),
                    component.rooms().stream().map(r -> r.sub(start.getSource())).collect(Collectors.toSet()),
                    component.exits().keySet().stream().collect(Collectors.toMap(r -> r.sub(start.getSource()), component.exits()::get))
            );
        }

        public MazePassage destination()
        {
            return destination;
        }

        @Nonnull
        public Function<MazePassage, C> exits()
        {
            return exits::get;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Ability<?> ability = (Ability<?>) o;

            if (!destination.equals(ability.destination)) return false;
            if (!start.equals(ability.start)) return false;
            if (!rooms.equals(ability.rooms)) return false;
            return exits.equals(ability.exits);
        }

        @Override
        public int hashCode()
        {
            int result = destination.hashCode();
            result = 31 * result + start.hashCode();
            result = 31 * result + rooms.hashCode();
            result = 31 * result + exits.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return "Ability{" +
                    "destination=" + destination +
                    "expect=" + start +
                    ", rooms=" + rooms +
                    ", exits=" + exits +
                    '}';
        }
    }

    private class ConnectionPoint
    {
        public final Set<MazePassage> traversed = new HashSet<>();
        public final List<Set<MazePassage>> order = new ArrayList<>();

        @SafeVarargs
        public ConnectionPoint(Collection<MazePassage>... points)
        {
            Arrays.stream(points).forEach(traversed::addAll);
        }

        public void reverseStep()
        {
            traversed.removeAll(order.remove(order.size() - 1));
        }
    }
}
