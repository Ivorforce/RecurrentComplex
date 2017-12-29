/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature;

import ivorius.reccomplex.RecurrentComplex;

import java.util.ArrayDeque;
import java.util.Deque;

public class RCWorldgenMonitor
{
    protected final static Deque<String> actions = new ArrayDeque<>();

    public static void start(String action)
    {
        actions.push(action);
    }

    public static void stop()
    {
        actions.pop();
    }

    public static void create()
    {
        WorldgenMonitor.create("Recurrent Complex", (p, d) -> {
            if (actions.size() > 0)
                RecurrentComplex.logger.warn("Cascading chunk generation happening while " + actions.peek());
        });
    }
}
