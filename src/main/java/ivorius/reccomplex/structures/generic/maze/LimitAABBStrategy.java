/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import ivorius.ivtoolkit.maze.components.*;

/**
 * Created by lukas on 16.04.15.
 */
public class LimitAABBStrategy<M extends MazeComponent<C>, C> implements MazeComponentPlacementStrategy<M, C>
{
    private int[] roomNumbers;

    public LimitAABBStrategy(int[] roomNumbers)
    {
        this.roomNumbers = roomNumbers;
    }

    public boolean isRoomContained(MazeRoom input)
    {
        for (int i = 0; i < input.getDimensions(); i++)
            if (input.getCoordinate(i) < 0 || input.getCoordinate(i) >= roomNumbers[i])
                return false;
        return true;
    }

    @Override
    public boolean canPlace(ShiftedMazeComponent<M, C> component)
    {
        return Iterables.all(component.rooms(), new Predicate<MazeRoom>()
        {
            @Override
            public boolean apply(MazeRoom input)
            {
                return isRoomContained(input);
            }
        });
    }

    @Override
    public boolean shouldContinue(MazeRoom dest, MazeRoom source, C c)
    {
        return isRoomContained(dest);
    }
}
