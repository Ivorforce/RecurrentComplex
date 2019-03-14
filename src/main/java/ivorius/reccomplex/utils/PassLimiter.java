/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import java.util.function.Supplier;

public class PassLimiter<E extends Exception>
{
    private int count = 0;
    private int max;
    private Supplier<E> thrower;

    public PassLimiter(Supplier<E> thrower, int max)
    {
        this.thrower = thrower;
        this.max = max;
    }

    public void error() throws E
    {
        throw thrower.get();
    }

    public void add(int count) throws E
    {
        this.count += count;

        if (this.count > max) {
            error();
        }
    }

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }
}
