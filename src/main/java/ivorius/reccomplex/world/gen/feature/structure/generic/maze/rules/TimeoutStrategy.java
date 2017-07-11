/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules;

import ivorius.ivtoolkit.maze.components.MazePredicate;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.maze.components.MorphingMazeComponent;
import ivorius.ivtoolkit.maze.components.ShiftedMazeComponent;

/**
 * Created by lukas on 16.04.15.
 */
public class TimeoutStrategy<C> implements MazePredicate<C>
{
    private Long startTime;
    private long limit;

    public TimeoutStrategy(long limit)
    {
        this.limit = limit;
    }

    @Override
    public boolean canPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {
        return true;
    }

    @Override
    public void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {

    }

    @Override
    public void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {
        if (startTime == null)
            startTime = System.currentTimeMillis();
        else if (System.currentTimeMillis() - startTime > limit)
            throw new TimeoutException(limit);
    }

    @Override
    public void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {

    }

    @Override
    public void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<?, C> component)
    {

    }

    @Override
    public boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c)
    {
        return true;
    }

    public static class TimeoutException extends RuntimeException
    {
        public final long time;

        public TimeoutException(long time)
        {
            this.time = time;
        }
    }
}
