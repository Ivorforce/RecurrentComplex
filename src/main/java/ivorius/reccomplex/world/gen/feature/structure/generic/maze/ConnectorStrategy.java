/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze;

import ivorius.ivtoolkit.maze.components.ConnectionStrategy;
import ivorius.ivtoolkit.maze.components.MazePassage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by lukas on 16.04.15.
 */
public class ConnectorStrategy implements ConnectionStrategy<Connector>
{
    public static final String DEFAULT_WALL = "Wall";
    public static final String DEFAULT_PATH = "Path";

    public float connect(@Nonnull MazePassage connection, @Nullable Connector existing, @Nonnull Connector add)
    {
        return add.accepts(existing) ? 1 : -1;
    }
}
