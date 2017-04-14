/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules;

import ivorius.ivtoolkit.maze.components.*;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by lukas on 16.04.15.
 */
public class BlockedConnectorStrategy<C> implements MazePredicate<C>
{
    @Nonnull
    private Collection<C> ignoredConnections;

    public BlockedConnectorStrategy(@Nonnull Collection<C> ignoredConnections)
    {
        this.ignoredConnections = ignoredConnections;
    }

    @Override
    public boolean canPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {
        return true;
    }

    @Override
    public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {

    }

    @Override
    public void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {

    }

    @Override
    public void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {

    }

    @Override
    public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {

    }

    @Override
    public boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c)
    {
        return !ignoredConnections.contains(c);
    }
}
