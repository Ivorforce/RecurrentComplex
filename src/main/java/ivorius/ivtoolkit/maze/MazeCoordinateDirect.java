/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.maze;

/**
 * Created by lukas on 23.06.14.
 */
public class MazeCoordinateDirect implements MazeCoordinate
{
    public int[] coordinates;

    public MazeCoordinateDirect(int... coordinates)
    {
        this.coordinates = coordinates;
    }

    @Override
    public int[] getMazeCoordinates()
    {
        return coordinates;
    }
}
