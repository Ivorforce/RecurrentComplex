/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by lukas on 16.04.15.
 */
public abstract class Connector
{
    @Nonnull
    public final String id;

    public Connector(@Nonnull String id)
    {
        this.id = id;
    }

    public abstract boolean accepts(@Nullable Connector c);

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Connector connector = (Connector) o;

        return id.equals(connector.id);

    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public String toString()
    {
        return id;
    }
}
