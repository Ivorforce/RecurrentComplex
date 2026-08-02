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

package ivorius.reccomplex.structures.generic.maze.components;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by lukas on 23.06.14.
 */
public class MazeRooms
{
    public static MazeRoom rotated(MazeRoom room, AxisAlignedTransform2D transform, int[] size)
    {
        if (room.getDimensions() < 3)
            throw new IllegalArgumentException();

        int[] roomPosition = room.getCoordinates();
        int[] transformedRoom = transform.apply(roomPosition, size);

        roomPosition[0] = transformedRoom[0];
        roomPosition[1] = transformedRoom[1];
        roomPosition[2] = transformedRoom[2];

        return new MazeRoom(roomPosition);
    }

    public static Stream<MazeRoom> neighbors(final MazeRoom room, IntStream dimensions)
    {
        return dimensions.mapToObj(d -> IntStream.of(1, -1).mapToObj(m -> room.addInDimension(d, m))).flatMap(t -> t);
    }

    public static Stream<MazeRoom> neighbors(MazeRoom room)
    {
        return neighbors(room, IntStream.range(0, room.getDimensions()));
    }

    public static Stream<MazePassage> neighborPassages(final MazeRoom room, IntStream dimensions)
    {
        return neighbors(room, dimensions).map(r -> new MazePassage(room, r));
    }

    public static Stream<MazePassage> neighborPassages(MazeRoom room)
    {
        return neighbors(room).map(r -> new MazePassage(room, r));
    }
}
