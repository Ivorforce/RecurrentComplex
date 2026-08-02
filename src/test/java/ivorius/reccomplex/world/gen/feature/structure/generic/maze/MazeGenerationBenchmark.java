/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.components.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.BlockedConnectorStrategy;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.LimitAABBStrategy;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.ReachabilityStrategy;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Headless driver for the maze connector, for benchmarking and for catching behaviour changes.
 * <p>
 * Deliberately touches no Minecraft classes, so it runs as a plain unit test without bootstrapping
 * Forge. It builds a synthetic component set rather than loading structures, so results stay
 * comparable across versions.
 * <p>
 * Run {@link #main} for a timing table; see MazeGenerationTest for the assertions.
 * <p>
 * Created by lukas on 02.08.26.
 */
public class MazeGenerationBenchmark
{
    public static final ConnectorFactory FACTORY = new ConnectorFactory();
    public static final Connector WALL = FACTORY.get(ConnectorStrategy.DEFAULT_WALL);
    public static final Connector PATH = FACTORY.get(ConnectorStrategy.DEFAULT_PATH);
    public static final Set<Connector> BLOCKED = Collections.singleton(WALL);

    /**
     * Which tile shapes may be used, as a bitmask over the number of open sides.
     * Restricting this starves the connector of options and forces backtracking, which is where
     * generation gets slow.
     */
    public static final int ALL_TILES = degrees(1, 2, 3, 4);

    public static int degrees(int... openSides)
    {
        int mask = 0;
        for (int i : openSides)
            mask |= 1 << i;
        return mask;
    }

    public static Result run(int width, int depth, int tiles, int reversesPerRoom, long seed)
    {
        return run(width, depth, components(tiles), reversesPerRoom, seed, false);
    }

    /**
     * @param preventConnection runs the reachability rule in its opposite mode, where the two
     *                          openings must end up unreachable from one another.
     */
    public static Result run(int width, int depth, List<Piece> components, int reversesPerRoom, long seed, boolean preventConnection)
    {
        MazePassage entry = new MazePassage(new MazeRoom(-1, 0, 0), new MazeRoom(0, 0, 0));
        MazePassage exit = new MazePassage(new MazeRoom(width, 0, depth - 1), new MazeRoom(width - 1, 0, depth - 1));

        // A shell that joins up its own openings would connect them whatever the rule decides
        MorphingMazeComponent<Connector> maze = shell(width, depth, Arrays.asList(entry, exit), !preventConnection);
        ConnectorStrategy strategy = new ConnectorStrategy();

        int[] bounds = new int[]{width, 1, depth};
        List<Collection<MazePassage>> points = Arrays.asList(Collections.singleton(entry), Collections.singleton(exit));

        List<MazePredicate<Connector>> predicates = new ArrayList<>();
        if (preventConnection)
            predicates.add(ReachabilityStrategy.preventConnection(points, new LimitAABBStrategy<>(bounds), strategy));
        else
            predicates.add(ReachabilityStrategy.connect(points, new LimitAABBStrategy<>(bounds),
                    ReachabilityStrategy.compileAbilities(components), strategy));
        predicates.add(new LimitAABBStrategy<>(bounds));
        predicates.add(new BlockedConnectorStrategy<>(BLOCKED));

        Counting<Connector> counting = new Counting<>(new MazePredicateMany<>(predicates));

        long start = System.nanoTime();
        List<PlacedMazeComponent<Piece, Connector>> placed = MazeComponentConnector.connect(maze, components,
                strategy, counting, new Random(seed), reversesPerRoom * width * depth);
        long nanos = System.nanoTime() - start;

        return new Result(layout(placed), placed.size(), connects(maze, entry, exit),
                counting.canPlaceCalls, counting.canPlaceTrue, counting.reversals, nanos, counting.canPlaceNanos);
    }

    /**
     * Every single-room piece with an allowed number of open horizontal sides, walled elsewhere.
     */
    public static List<Piece> components(int tiles)
    {
        List<Piece> pieces = new ArrayList<>();
        MazeRoom origin = new MazeRoom(0, 0, 0);
        List<MazePassage> sides = Arrays.asList(
                new MazePassage(origin, new MazeRoom(1, 0, 0)),
                new MazePassage(origin, new MazeRoom(-1, 0, 0)),
                new MazePassage(origin, new MazeRoom(0, 0, 1)),
                new MazePassage(origin, new MazeRoom(0, 0, -1)));

        for (int mask = 1; mask < 1 << sides.size(); mask++)
        {
            if ((tiles & (1 << Integer.bitCount(mask))) == 0)
                continue;

            Map<MazePassage, Connector> exits = new LinkedHashMap<>();
            for (int side = 0; side < sides.size(); side++)
                exits.put(sides.get(side), (mask & (1 << side)) != 0 ? PATH : WALL);

            Set<MazeRoom> rooms = Collections.singleton(origin);
            addMissingExits(rooms, exits, WALL);
            pieces.add(new Piece("R" + mask, rooms, exits, reachability(exits)));
        }

        return pieces;
    }

    /**
     * Room offsets per piece shape: single, domino along x, domino along z, L, square. The single
     * has to be in the mix, since without it the connector cannot fill a one-room gap and gives up
     * before placing anything.
     */
    public static final int[][][] SHAPES = {
            {{0, 0, 0}},
            {{0, 0, 0}, {1, 0, 0}},
            {{0, 0, 0}, {0, 0, 1}},
            {{0, 0, 0}, {1, 0, 0}, {0, 0, 1}},
            {{0, 0, 0}, {1, 0, 0}, {0, 0, 1}, {1, 0, 1}},
    };

    /**
     * Pieces spanning up to four rooms, with pseudo-random openings.
     * <p>
     * Worth covering separately from {@link #components}: a piece covering several rooms carries
     * reachability between its own exits, so taking one back has to undo edges rather than just
     * rooms and exits. Single-room pieces never produce that case.
     */
    public static List<Piece> multiRoomComponents(int variantsPerShape, long seed)
    {
        Random random = new Random(seed);
        List<Piece> pieces = new ArrayList<>();

        for (int shape = 0; shape < SHAPES.length; shape++)
        {
            Set<MazeRoom> rooms = new LinkedHashSet<>();
            for (int[] offset : SHAPES[shape])
                rooms.add(new MazeRoom(offset));

            List<MazePassage> boundary = new ArrayList<>();
            for (MazeRoom room : rooms)
                MazeRooms.neighborPassages(room)
                        .filter(passage -> !(rooms.contains(passage.getLeft()) && rooms.contains(passage.getRight())))
                        .filter(passage -> passage.getRight().getCoordinate(1) == 0) // Horizontal only
                        .forEach(boundary::add);

            for (int variant = 0; variant < variantsPerShape; variant++)
            {
                Map<MazePassage, Connector> exits = new LinkedHashMap<>();
                int open = 0;

                for (MazePassage passage : boundary)
                {
                    boolean isOpen = random.nextInt(3) > 0;
                    open += isOpen ? 1 : 0;
                    exits.put(passage, isOpen ? PATH : WALL);
                }

                if (open < 2)
                    continue; // A dead end makes the search fail outright rather than backtrack

                addMissingExits(rooms, exits, WALL);
                pieces.add(new Piece("S" + shape + "V" + variant, rooms, exits, reachability(exits)));
            }
        }

        return pieces;
    }

    /**
     * The enclosing wall, with the given openings punched into it for a rule to connect.
     */
    public static MorphingMazeComponent<Connector> shell(int width, int depth, Collection<MazePassage> openings)
    {
        return shell(width, depth, openings, true);
    }

    public static MorphingMazeComponent<Connector> shell(int width, int depth, Collection<MazePassage> openings, boolean connectOpenings)
    {
        Set<MazeRoom> rooms = new LinkedHashSet<>();
        for (int x = -1; x <= width; x++)
            for (int z = -1; z <= depth; z++)
                for (int y = -1; y <= 1; y++)
                    if (x < 0 || x >= width || z < 0 || z >= depth || y != 0)
                        rooms.add(new MazeRoom(x, y, z));

        Map<MazePassage, Connector> exits = new LinkedHashMap<>();
        openings.forEach(opening -> exits.put(opening, PATH));
        addMissingExits(rooms, exits, WALL);

        MorphingMazeComponent<Connector> maze = new SetMazeComponent<>();
        maze.add(new SetMazeComponent<>(rooms, exits, reachability(exits, connectOpenings)));
        return maze;
    }

    /**
     * Mirrors WorldGenMaze.addMissingExits, which we can't call since it drags in Minecraft.
     */
    protected static void addMissingExits(Set<MazeRoom> rooms, Map<MazePassage, Connector> exits, Connector connector)
    {
        for (MazeRoom room : rooms)
            MazeRooms.neighborPassages(room)
                    .filter(passage -> !exits.containsKey(passage)
                            && !(rooms.contains(passage.getLeft()) && rooms.contains(passage.getRight())))
                    .forEach(passage -> exits.put(passage, connector));
    }

    protected static Multimap<MazePassage, MazePassage> reachability(Map<MazePassage, Connector> exits)
    {
        return reachability(exits, true);
    }

    protected static Multimap<MazePassage, MazePassage> reachability(Map<MazePassage, Connector> exits, boolean connectOpenings)
    {
        Multimap<MazePassage, MazePassage> reachability = HashMultimap.create();

        Set<MazePassage> open = exits.keySet().stream().filter(passage -> !BLOCKED.contains(exits.get(passage)))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (connectOpenings)
            SetMazeComponent.connectAll(open, reachability);
        open.forEach(passage -> reachability.put(passage, passage.inverse())); // Leading outside

        return reachability;
    }

    /**
     * Walks the finished maze to see whether the two openings actually ended up connected - the
     * property ReachabilityStrategy exists to guarantee, and the one an unsound optimization of it
     * would silently break.
     */
    protected static boolean connects(MazeComponent<Connector> maze, MazePassage from, MazePassage to)
    {
        Set<MazePassage> visited = new HashSet<>(Collections.singleton(from));
        Deque<MazePassage> dirty = new ArrayDeque<>(Collections.singleton(from));

        MazePassage passage;
        while ((passage = dirty.pollFirst()) != null)
        {
            if (passage.equals(to) || passage.equals(to.inverse()))
                return true;

            for (MazePassage dest : maze.reachability().get(passage))
                if (visited.add(dest))
                    dirty.addLast(dest);
        }

        return false;
    }

    /**
     * A stable fingerprint of the generated maze. Two runs that place the same components in the
     * same order at the same positions agree; anything else does not.
     */
    protected static String layout(List<PlacedMazeComponent<Piece, Connector>> placed)
    {
        StringBuilder builder = new StringBuilder();
        for (PlacedMazeComponent<Piece, Connector> component : placed)
            builder.append(component.component().name).append('@')
                    .append(Arrays.toString(component.shift().getCoordinates())).append(';');
        return String.format("%08x", builder.toString().hashCode());
    }

    public static void main(String[] args)
    {
        int size = args.length > 0 ? Integer.parseInt(args[0]) : 20;
        int trials = args.length > 1 ? Integer.parseInt(args[1]) : 5;
        int warmup = args.length > 2 ? Integer.parseInt(args[2]) : 3;

        for (int[] setup : new int[][]{{ALL_TILES, 3}, {degrees(2, 3), 3}})
        {
            for (int i = 0; i < warmup; i++)
                run(size, size, setup[0], setup[1], i);

            long nanos = 0, predicateNanos = 0, calls = 0;
            for (long seed = 0; seed < trials; seed++)
            {
                Result result = run(size, size, setup[0], setup[1], seed);
                nanos += result.nanos;
                predicateNanos += result.predicateNanos;
                calls += result.canPlaceCalls;

                System.out.printf("  seed=%-3d rooms=%-4d canPlace=%-8d reversals=%-7d %7.1f ms  connected=%-5b layout=%s%n",
                        seed, result.placed, result.canPlaceCalls, result.reversals, result.nanos / 1e6,
                        result.connected, result.layout);
            }

            System.out.printf("%dx%d tiles=%s: %.1f ms over %d mazes, %.0f%% in predicates, %d canPlace calls%n%n",
                    size, size, Integer.toBinaryString(setup[0]), nanos / 1e6, trials,
                    100.0 * predicateNanos / nanos, calls);
        }
    }

    public static class Result
    {
        public final String layout;
        public final int placed;
        public final boolean connected;
        public final long canPlaceCalls;
        public final long canPlaceAccepted;
        /** How often the search took a placement back - i.e. how hard this run exercised undo. */
        public final long reversals;
        public final long nanos;
        public final long predicateNanos;

        public Result(String layout, int placed, boolean connected, long canPlaceCalls, long canPlaceAccepted, long reversals, long nanos, long predicateNanos)
        {
            this.layout = layout;
            this.placed = placed;
            this.connected = connected;
            this.canPlaceCalls = canPlaceCalls;
            this.canPlaceAccepted = canPlaceAccepted;
            this.reversals = reversals;
            this.nanos = nanos;
            this.predicateNanos = predicateNanos;
        }
    }

    /**
     * Counts and times predicate work, so a run can be compared across machines by call count
     * rather than by wall clock.
     */
    public static class Counting<C> implements MazePredicate<C>
    {
        public final MazePredicate<C> delegate;

        public long canPlaceCalls;
        public long canPlaceTrue;
        public long canPlaceNanos;
        public long reversals;

        public Counting(MazePredicate<C> delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public boolean canPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
        {
            canPlaceCalls++;

            long start = System.nanoTime();
            boolean canPlace = delegate.canPlace(maze, component);
            canPlaceNanos += System.nanoTime() - start;

            if (canPlace) canPlaceTrue++;
            return canPlace;
        }

        @Override
        public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
        {
            delegate.willPlace(maze, component);
        }

        @Override
        public void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
        {
            delegate.didPlace(maze, component);
        }

        @Override
        public void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
        {
            reversals++; // Exactly one per taken-back placement
            delegate.willUnplace(maze, component);
        }

        @Override
        public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
        {
            delegate.didUnplace(maze, component);
        }

        @Override
        public boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c)
        {
            return delegate.isDirtyConnection(dest, source, c);
        }
    }

    public static class Piece implements WeightedMazeComponent<Connector>
    {
        public final String name;
        public final Set<MazeRoom> rooms;
        public final Map<MazePassage, Connector> exits;
        public final Multimap<MazePassage, MazePassage> reachability;

        public Piece(String name, Set<MazeRoom> rooms, Map<MazePassage, Connector> exits, Multimap<MazePassage, MazePassage> reachability)
        {
            this.name = name;
            this.rooms = rooms;
            this.exits = exits;
            this.reachability = reachability;
        }

        @Override
        public double getWeight()
        {
            return 1;
        }

        @Override
        public Set<MazeRoom> rooms()
        {
            return rooms;
        }

        @Override
        public Map<MazePassage, Connector> exits()
        {
            return exits;
        }

        @Override
        public Multimap<MazePassage, MazePassage> reachability()
        {
            return reachability;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
