/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import ivorius.ivtoolkit.maze.components.ConnectionStrategy;
import ivorius.ivtoolkit.maze.components.MazePassage;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 16.04.15.
 */
public class ConnectorStrategy implements ConnectionStrategy<Connector>
{
    public static final String DEFAULT_WALL = "Wall";
    public static final String DEFAULT_PATH = "Path";

    public float connect(@Nonnull MazePassage connection, Connector a, Connector b)
    {
        return (a != null ? a.accepts(b) : (b == null || b.accepts(null))) ? 1 : -1;
    }
}
