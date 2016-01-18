/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.maze.components.MazeRoomConnection;
import ivorius.ivtoolkit.maze.components.WeightedMazeComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;

/**
 * Created by lukas on 16.04.15.
 */
public class MazeComponentStructure<C> implements WeightedMazeComponent<C>
{
    public final ImmutableSet<MazeRoom> rooms;
    public final ImmutableMap<MazeRoomConnection, C> exits;
    public final ImmutableSet<Pair<MazeRoomConnection, MazeRoomConnection>> reachability;

    public double weight;

    public String structureID;
    public AxisAlignedTransform2D transform;
    private int[] size;

    public MazeComponentStructure(double weight, String structureID, AxisAlignedTransform2D transform, ImmutableSet<MazeRoom> rooms, ImmutableMap<MazeRoomConnection, C> exits, ImmutableSet<Pair<MazeRoomConnection, MazeRoomConnection>> reachability)
    {
        this.weight = weight;
        this.structureID = structureID;
        this.transform = transform;

        this.rooms = rooms;
        this.exits = exits;
        this.reachability = reachability;
    }

    @Override
    public double getWeight()
    {
        return weight;
    }

    @Override
    public Set<MazeRoom> rooms()
    {
        return rooms;
    }

    @Override
    public Map<MazeRoomConnection, C> exits()
    {
        return exits;
    }

    @Override
    public Set<Pair<MazeRoomConnection, MazeRoomConnection>> reachability()
    {
        return reachability;
    }

    public int[] getSize()
    {
        int[] lowest = null;
        int[] highest = null;

        for (MazeRoom room : rooms)
        {
            for (int i = 0; i < room.getDimensions(); i++)
            {
                if (lowest == null)
                {
                    lowest = room.getCoordinates();
                    highest = room.getCoordinates();
                }
                else
                {
                    if (room.getCoordinate(i) < lowest[i])
                        lowest[i] = room.getCoordinate(i);
                    else if (room.getCoordinate(i) > highest[i])
                        highest[i] = room.getCoordinate(i);
                }
            }
        }

        if (lowest == null)
            throw new UnsupportedOperationException();

        int[] size = IvVecMathHelper.sub(highest, lowest);
        for (int i = 0; i < size.length; i++)
            size[i]++;

        return size;
    }
}
