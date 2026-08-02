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
}
