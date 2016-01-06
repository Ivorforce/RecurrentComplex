/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.common.collect.Multimap;
import ivorius.ivtoolkit.maze.classic.MazeCoordinate;
import ivorius.ivtoolkit.maze.components.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Created by lukas on 05.10.15.
 */
public class ReachabilityStrategy<M extends MazeComponent<C>, C> implements MazePredicate<M, C>
{
    private final Set<C> blockingConnections = new HashSet<>();

    private MorphingMazeComponent<C> maze;
    private MazeCoordinate start;
    private MazeCoordinate end;

    private boolean connected;
    private Multimap<MazeRoom, MazeRoomConnection> connections;

    public ReachabilityStrategy(MorphingMazeComponent<C> maze, MazeCoordinate start, MazeCoordinate end, Set<C> blockingConnections)
    {
        this.maze = maze;
        this.start = start;
        this.end = end;
        this.blockingConnections.addAll(blockingConnections);

        // Add all dirty exits
        for (Map.Entry<MazeRoomConnection, C> entry : maze.exits().entrySet())
        {
            if (!blocksReachability(entry.getValue()))
            {
                MazeRoomConnection connection = entry.getKey();
                connections.put(connection.getLeft(), connection);
                connections.put(connection.getRight(), connection);
            }
        }
    }

    private boolean blocksReachability(C c)
    {
        return blockingConnections.contains(c);
    }

    @Override
    public boolean canPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        if (connected)
            return true;

        // Find all dirty exits

        Set<MazeRoom> dirtyExits = new HashSet<>();

        // Check if any dirty exit can connect to the destination

        Set<MazeRoom> checked = dirtyExits;
        Stack<MazeRoom> toCheck = new Stack<>();
        toCheck.addAll(dirtyExits);

        while (!toCheck.isEmpty())
        {
            MazeRoom room = toCheck.pop();

            // Run through connections
            for (MazeRoomConnection connection : connections.get(room))
            {
                MazeRoom other = connection.getLeft().equals(room) ? connection.getRight() : connection.getLeft();
                if (!checked.contains(other))
                {
                    toCheck.add(other);
                    checked.add(other);
                }
            }

            // Check free neighbors
        }

        return false;
    }

    @Override
    public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
        for (Map.Entry<MazeRoomConnection, C> entry : component.exits().entrySet())
        {
            if (!blocksReachability(entry.getValue()))
            {
                MazeRoomConnection connection = entry.getKey();

                // Add all exits
                connections.put(connection.getLeft(), connection);
                connections.put(connection.getRight(), connection);

                // Add all room interconnections
                for (MazeRoomConnection connection2 : component.exits().keySet())
                {
                    // It doesn't matter which two rooms are connected because the generic exit also exists
                    if (!connection2.equals(connection))
                    {
                        MazeRoomConnection interconnect = new MazeRoomConnection(connection.getLeft(), connection2.getLeft());
                        connections.put(connection.getLeft(), interconnect);
                        connections.put(connection.getRight(), interconnect);
                    }
                }
            }
        }
    }

    @Override
    public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {
         // TODO
    }

    @Override
    public boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c)
    {
        return true;
    }
}
