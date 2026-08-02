/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static ivorius.reccomplex.world.gen.feature.structure.generic.maze.MazeGenerationBenchmark.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Guards maze generation against accidental behaviour changes while it is being optimized.
 * <p>
 * An optimization that only makes the search faster must leave both the layout and the call count
 * untouched. If one of these fails after a change that was meant to be a pure speedup, the change
 * altered the search - which for a reachability rule means mazes that are unsolvable or that fail
 * to generate at all.
 * <p>
 * These are golden values, not derived truths: regenerate them with
 * {@link MazeGenerationBenchmark#main} if the search itself is deliberately changed.
 * <p>
 * Created by lukas on 02.08.26.
 */
public class MazeGenerationTest
{
    public static final int REVERSES_PER_ROOM = 3;

    /**
     * (width, tiles, seed) -> layout fingerprint, canPlace calls.
     */
    public static Map<String, long[]> golden()
    {
        Map<String, long[]> golden = new LinkedHashMap<>();
        golden.put("8:" + ALL_TILES + ":0", new long[]{0xf91f625fL, 43});
        golden.put("8:" + ALL_TILES + ":1", new long[]{0x677b088cL, 47});
        golden.put("14:" + ALL_TILES + ":0", new long[]{0xa8113634L, 180});
        golden.put("14:" + degrees(2, 3) + ":0", new long[]{0x04282b0aL, 730});
        golden.put("14:" + degrees(2) + ":0", new long[]{0xa57ef4a4L, 85});
        return golden;
    }

    @Test
    public void generationIsDeterministic()
    {
        for (int tiles : new int[]{ALL_TILES, degrees(2, 3)})
        {
            Result first = run(12, 12, tiles, REVERSES_PER_ROOM, 42);
            Result second = run(12, 12, tiles, REVERSES_PER_ROOM, 42);

            assertEquals("layout differs between runs of the same seed", first.layout, second.layout);
            assertEquals("search differs between runs of the same seed", first.canPlaceCalls, second.canPlaceCalls);
        }
    }

    @Test
    public void generationMatchesGoldenLayouts()
    {
        golden().forEach((key, expected) ->
        {
            String[] parts = key.split(":");
            int size = Integer.parseInt(parts[0]);

            Result result = run(size, size, Integer.parseInt(parts[1]), REVERSES_PER_ROOM, Long.parseLong(parts[2]));

            assertEquals("layout changed for " + key, String.format("%08x", expected[0]), result.layout);
            assertEquals("search size changed for " + key, expected[1], result.canPlaceCalls);
        });
    }

    /**
     * The end-to-end property, independent of any fingerprint: whatever the search does, the two
     * openings must actually be walkable from one to the other in the finished maze. A reachability
     * optimization that answers from a stale cache passes the golden tests only if it happens not to
     * change the layout, but it fails here.
     */
    @Test
    public void reachabilityRuleConnectsTheMaze()
    {
        for (int tiles : new int[]{ALL_TILES, degrees(2, 3), degrees(2)})
            for (int size : new int[]{12, 20})
                for (long seed = 0; seed < 8; seed++)
                    assertTrue(String.format("maze %dx%d tiles=%d seed=%d is not connected", size, size, tiles, seed),
                            run(size, size, tiles, REVERSES_PER_ROOM, seed).connected);
    }

    /**
     * The same guard over pieces spanning two to four rooms. Single-room pieces leave the connector's
     * undo bookkeeping barely exercised, since a piece can then never close an exit one of its own
     * rooms opened.
     * <p>
     * (variants, piece seed, size, seed) -> layout fingerprint, canPlace calls, reversals.
     */
    public static Map<String, long[]> multiRoomGolden()
    {
        Map<String, long[]> golden = new LinkedHashMap<>();
        golden.put("3:2:10:1", new long[]{0x54769254L, 43, 0});     // Placed clean, no undo at all
        golden.put("3:3:10:1", new long[]{0x6a5cdfa3L, 47, 7});
        golden.put("3:3:16:1", new long[]{0x46d530f1L, 114, 7});
        golden.put("3:4:16:2", new long[]{0x91e0d539L, 902, 768});  // Backtracked until out of budget
        golden.put("6:3:16:0", new long[]{0x598b9bb8L, 888, 768});
        golden.put("6:4:16:1", new long[]{0xfc6449f8L, 895, 768});
        return golden;
    }

    @Test
    public void multiRoomGenerationMatchesGoldenLayouts()
    {
        long reversals = 0;

        for (Map.Entry<String, long[]> entry : multiRoomGolden().entrySet())
        {
            String[] parts = entry.getKey().split(":");
            long[] expected = entry.getValue();
            int size = Integer.parseInt(parts[2]);

            Result result = run(size, size, multiRoomComponents(Integer.parseInt(parts[0]), Long.parseLong(parts[1])),
                    REVERSES_PER_ROOM, Long.parseLong(parts[3]), false);

            assertEquals("layout changed for " + entry.getKey(), String.format("%08x", expected[0]), result.layout);
            assertEquals("search size changed for " + entry.getKey(), expected[1], result.canPlaceCalls);
            assertEquals("undo count changed for " + entry.getKey(), expected[2], result.reversals);

            reversals += result.reversals;
        }

        // Guards the guard: golden values over runs that never backtracked would prove nothing
        assertTrue("multi-room cases no longer exercise undo", reversals > 1000);
    }

    /**
     * The reachability rule's other mode. Where {@link #reachabilityRuleConnectsTheMaze} demands the
     * openings end up joined, this one demands the search never joins them - so it fails for an
     * optimization that under-reports reachability just as the other fails for over-reporting.
     */
    @Test
    public void preventConnectionRuleKeepsTheMazeApart()
    {
        for (int tiles : new int[]{ALL_TILES, degrees(2, 3)})
            for (int size : new int[]{12, 20})
                for (long seed = 0; seed < 8; seed++)
                    assertFalse(String.format("maze %dx%d tiles=%d seed=%d connected despite the rule", size, size, tiles, seed),
                            run(size, size, components(tiles), REVERSES_PER_ROOM, seed, true).connected);
    }
}
