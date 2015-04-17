/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import javax.annotation.Nullable;

/**
 * Created by lukas on 16.04.15.
 */
public abstract class Connector
{
    public final String id;

    public Connector(String id)
    {
        this.id = id;
    }

    public abstract boolean accepts(@Nullable Connector c);

    @Override
    public String toString()
    {
        return id;
    }
}
