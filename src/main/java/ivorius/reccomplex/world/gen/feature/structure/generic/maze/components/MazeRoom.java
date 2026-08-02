/*
 * Copyright 2015 Lukas Tenbrink
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze.components;

import ivorius.ivtoolkit.math.IvVecMathHelper;
import net.minecraft.nbt.NBTTagIntArray;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Created by lukas on 23.06.14.
 */
public class MazeRoom
{
    @Nonnull
    private final int[] coordinates;

    /**
     * Rooms are immutable and are the key type of every map in a maze search, so this is worth
     * paying for once rather than on every lookup.
     */
    private final int hash;

    public MazeRoom(@Nonnull int... coordinates)
    {
        this.coordinates = coordinates.clone();
        this.hash = Arrays.hashCode(this.coordinates);
    }

    public MazeRoom(NBTTagIntArray intArray)
    {
        this(intArray.getIntArray());
    }

    public int getDimensions()
    {
        return coordinates.length;
    }

    public int[] getCoordinates()
    {
        return coordinates.clone();
    }

    public int getCoordinate(int index)
    {
        return coordinates[index];
    }

    public MazeRoom add(MazeRoom room)
    {
        return new MazeRoom(IvVecMathHelper.add(coordinates, room.coordinates));
    }

    public MazeRoom addInDimension(int dimension, int count)
    {
        coordinates[dimension] += count;
        MazeRoom room = new MazeRoom(coordinates);
        coordinates[dimension] -= count; // Works because was copied
        return room;
    }

    public MazeRoom sub(MazeRoom room)
    {
        return new MazeRoom(IvVecMathHelper.sub(coordinates, room.coordinates));
    }

    public MazeRoom subInDimension(int dimension, int count)
    {
        coordinates[dimension] -= count;
        MazeRoom room = new MazeRoom(coordinates);
        coordinates[dimension] += count; // Works because was copied
        return room;
    }

    public MazeRoom negate()
    {
        int[] coords = getCoordinates();
        for (int i = 0; i < coords.length; i++)
            coords[i] = -coords[i];
        return new MazeRoom(coords);
    }

    public double length()
    {
        return IvVecMathHelper.length(coordinates);
    }

    public double distance(MazeRoom room)
    {
        return IvVecMathHelper.distance(coordinates, room.coordinates);
    }

    public double distanceSQ(MazeRoom room)
    {
        return IvVecMathHelper.distanceSQ(coordinates, room.coordinates);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MazeRoom mazeRoom = (MazeRoom) o;

        return hash == mazeRoom.hash && Arrays.equals(coordinates, mazeRoom.coordinates);

    }

    @Override
    public int hashCode()
    {
        return hash;
    }

    @Override
    public String toString()
    {
        return Arrays.toString(coordinates);
    }

    public NBTTagIntArray storeInNBT()
    {
        return new NBTTagIntArray(coordinates.clone());
    }
}
