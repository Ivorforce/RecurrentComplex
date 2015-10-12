/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import ivorius.ivtoolkit.maze.components.*;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by lukas on 16.04.15.
 */
public class LimitAABBStrategy<M extends MazeComponent<C>, C> implements MazePredicate<M, C>
{
    @Nonnull
    private int[] roomNumbers;

    public LimitAABBStrategy(@Nonnull int[] roomNumbers)
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
    public boolean canPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
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
    public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component)
    {

    }

    @Override
    public boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c)
    {
        return isRoomContained(dest);
    }
}
