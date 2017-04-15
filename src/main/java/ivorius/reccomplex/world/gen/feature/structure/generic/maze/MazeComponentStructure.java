/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.components.MazePassage;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.maze.components.WeightedMazeComponent;
import ivorius.reccomplex.world.gen.feature.structure.VariableDomain;

import java.util.Map;
import java.util.Set;

/**
 * Created by lukas on 16.04.15.
 */
public class MazeComponentStructure<C> implements WeightedMazeComponent<C>
{
    public final ImmutableSet<MazeRoom> rooms;
    public final ImmutableMap<MazePassage, C> exits;
    public final ImmutableMultimap<MazePassage, MazePassage> reachability;

    public double weight;

    public String structureID;
    public VariableDomain variableDomain;
    public AxisAlignedTransform2D transform;

    public MazeComponentStructure(double weight, String structureID, VariableDomain variableDomain, AxisAlignedTransform2D transform, ImmutableSet<MazeRoom> rooms, ImmutableMap<MazePassage, C> exits, ImmutableMultimap<MazePassage, MazePassage> reachability)
    {
        this.weight = weight;
        this.variableDomain = variableDomain;
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
    public Map<MazePassage, C> exits()
    {
        return exits;
    }

    @Override
    public Multimap<MazePassage, MazePassage> reachability()
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

    @Override
    public String toString()
    {
        return "MazeComponentStructure{" +
                "structureID='" + structureID + '\'' +
                ", variableDomain=" + variableDomain +
                ", transform=" + transform +
                '}';
    }
}
