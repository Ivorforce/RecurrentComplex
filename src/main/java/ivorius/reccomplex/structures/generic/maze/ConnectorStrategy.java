/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import ivorius.ivtoolkit.maze.components.ConnectionStrategy;
import ivorius.ivtoolkit.maze.components.MazeRoomConnection;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 16.04.15.
 */
public class ConnectorStrategy implements ConnectionStrategy<Connector>
{
    @Override
    public boolean connect(@Nonnull MazeRoomConnection connection, Connector a, Connector b)
    {
        return a != null ? a.accepts(b) : b == null || b.accepts(null);
    }
}
