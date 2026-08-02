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

import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Created by lukas on 15.04.15.
 */
public class MazeRoomConnection extends Pair<MazeRoom, MazeRoom>
{
    private final MazeRoom left;
    private final MazeRoom right;

    public MazeRoomConnection(MazeRoom left, MazeRoom right)
    {
        boolean lower = lower(left, right);
        this.left = lower ? left : right;
        this.right = lower ? right : left;

        if (left.getDimensions() != right.getDimensions())
            throw new IllegalArgumentException();
    }

    private static boolean lower(MazeRoom left, MazeRoom right)
    {
        for (int i = 0; i < left.getDimensions(); i++)
            if (left.getCoordinate(i) != right.getCoordinate(i))
                return left.getCoordinate(i) < right.getCoordinate(i);

        return false; // Same
    }

    @Override
    public MazeRoom getLeft()
    {
        return left;
    }

    @Override
    public MazeRoom getRight()
    {
        return right;
    }

    public int getDimensions()
    {
        return left.getDimensions();
    }

    public MazeRoomConnection add(MazeRoom add)
    {
        return new MazeRoomConnection(left.add(add), right.add(add));
    }

    public MazeRoomConnection sub(MazeRoom sub)
    {
        return new MazeRoomConnection(left.sub(sub), right.sub(sub));
    }

    @Nullable
    public MazeRoom distance(MazeRoomConnection connection)
    {
        MazeRoom leftDist = connection.left.sub(left);
        return leftDist.equals(connection.right.sub(right)) ? leftDist : null;
    }

    public MazeRoomConnection normalize()
    {
        return new MazeRoomConnection(new MazeRoom(new int[getDimensions()]), right.sub(left));
    }

    public boolean has(MazeRoom room)
    {
        return Objects.equals(left, room) || Objects.equals(right, room);
    }

    @Override
    public MazeRoom setValue(MazeRoom value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MazeRoomConnection that = (MazeRoomConnection) o;

        if (left != null ? !left.equals(that.left) : that.left != null) return false;
        return right != null ? right.equals(that.right) : that.right == null;

    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (left != null ? left.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "MazeRoomConnection{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}
