/*
 * Copyright 2016 Lukas Tenbrink
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
 * Created by lukas on 28.03.16.
 */
public class MazePassage extends Pair<MazeRoom, MazeRoom>
{
    private final MazeRoom source;
    private final MazeRoom dest;

    /**
     * Passages are immutable and are the key type of every reachability map, so this is worth
     * paying for once rather than on every lookup.
     */
    private final int hash;

    public MazePassage(MazeRoom source, MazeRoom dest)
    {
        this.source = source;
        this.dest = dest;

        if (source.getDimensions() != dest.getDimensions())
            throw new IllegalArgumentException();

        int hash = super.hashCode();
        hash = 31 * hash + source.hashCode();
        this.hash = 31 * hash + dest.hashCode();
    }

    public MazeRoom getSource()
    {
        return source;
    }

    public MazeRoom getDest()
    {
        return dest;
    }

    @Override
    public MazeRoom getLeft()
    {
        return source;
    }

    @Override
    public MazeRoom getRight()
    {
        return dest;
    }

    public int getDimensions()
    {
        return source.getDimensions();
    }

    public MazePassage add(MazeRoom add)
    {
        return new MazePassage(source.add(add), dest.add(add));
    }

    public MazePassage sub(MazeRoom sub)
    {
        return new MazePassage(source.sub(sub), dest.sub(sub));
    }

    public MazeRoomConnection toConnection()
    {
        return new MazeRoomConnection(source, dest);
    }

    @Nullable
    public MazeRoom distance(MazePassage connection)
    {
        MazeRoom leftDist = connection.source.sub(source);
        return leftDist.equals(connection.dest.sub(dest)) ? leftDist : null;
    }

    @Nullable
    public MazeRoom inverseDistance(MazePassage connection)
    {
        MazeRoom leftDist = connection.source.sub(dest);
        return leftDist.equals(connection.dest.sub(source)) ? leftDist : null;
    }

    public MazePassage inverse()
    {
        return new MazePassage(dest, source);
    }

    public MazePassage normalize()
    {
        return new MazePassage(new MazeRoom(new int[getDimensions()]), dest.sub(source));
    }

    public boolean has(MazeRoom room)
    {
        return Objects.equals(source, room) || Objects.equals(dest, room);
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

        MazePassage that = (MazePassage) o;

        if (hash != that.hash) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        return dest != null ? dest.equals(that.dest) : that.dest == null;

    }

    @Override
    public int hashCode()
    {
        return hash;
    }

    @Override
    public String toString()
    {
        return "MazePassage{" +
                "source=" + source +
                ", dest=" + dest +
                '}';
    }
}
