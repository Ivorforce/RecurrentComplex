/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.maze;

import ivorius.ivtoolkit.maze.components.MazeComponent;
import ivorius.ivtoolkit.maze.components.MazePredicate;
import ivorius.ivtoolkit.maze.components.MazeRoom;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Function;

/**
 * Created by lukas on 30.01.17.
 */
public class MazeInterpreter
{
//    MazeInterpreter.print2D(maze, predicate, 0, Arrays.asList(triple.getLeft()))

    public static <C> void print2D(MazeComponent<C> component, MazePredicate<C> predicate, MazeRoom... mark)
    {
        print2D(component, predicate, r -> Arrays.asList(mark).contains(r) ? "O" : null);
    }

    public static <C> void print2D(MazeComponent<C> component, MazePredicate<C> predicate, Function<MazeRoom, String> marker)
    {
        int minY = component.rooms().stream().mapToInt(r -> r.getCoordinate(1)).min().orElseThrow(IllegalArgumentException::new);
        int maxY = component.rooms().stream().mapToInt(r -> r.getCoordinate(1)).max().orElseThrow(IllegalArgumentException::new);

        for (int y = minY; y <= maxY; y++)
            System.out.print(MazeInterpreter.toString(component, predicate, 0, 2, new MazeRoom(0, y, 0), marker));
    }

    public static <C> String toString(MazeComponent<C> component, @Nullable MazePredicate<C> predicate, int xDim, int zDim, MazeRoom pos, @Nullable Function<MazeRoom, String> marker)
    {
        int minX = component.rooms().stream().mapToInt(r -> r.getCoordinate(xDim)).min().orElseThrow(IllegalArgumentException::new) - 1;
        int minZ = component.rooms().stream().mapToInt(r -> r.getCoordinate(zDim)).min().orElseThrow(IllegalArgumentException::new) - 1;
        int maxX = component.rooms().stream().mapToInt(r -> r.getCoordinate(xDim)).max().orElseThrow(IllegalArgumentException::new) + 1;
        int maxZ = component.rooms().stream().mapToInt(r -> r.getCoordinate(zDim)).max().orElseThrow(IllegalArgumentException::new) + 1;

        StringBuilder builder = new StringBuilder();

        builder.append(" ");
        for (int x = minX; x <= maxX; x++)
            builder.append(((x % 10) + 10) % 10).append(" ");
        builder.append("\n");

        for (int z = minZ; z <= maxZ; z++)
        {
            builder.append(((z % 10) + 10) % 10);

            for (int x = minX; x <= maxX; x++)
            {
                MazeRoom mazeRoom = setInDimension(setInDimension(pos, zDim, z), xDim, x);

                String rep;
                String marked = marker != null ? marker.apply(mazeRoom) : null;
                if (marked != null)
                    rep = marked;
                else if (component.rooms().contains(mazeRoom))
                    rep = isExit(component, predicate, mazeRoom) ? "x" : "X";
                else
                    rep = isExit(component, predicate, mazeRoom) ? "-" : " ";

                builder.append(rep).append(" ");
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    protected static <C> boolean isExit(MazeComponent<C> component, @Nullable MazePredicate<C> predicate, MazeRoom mazeRoom)
    {
        return component.exits().keySet().stream()
                .filter(p -> p.getDest().equals(mazeRoom) || p.getSource().equals(mazeRoom))
                .anyMatch(p -> predicate != null && predicate.isDirtyConnection(p.getSource(), p.getDest(), component.exits().get(p)));
    }

    protected static MazeRoom setInDimension(MazeRoom pos, int dim, int val)
    {
        return pos.addInDimension(dim, val - pos.getCoordinate(dim));
    }
}
