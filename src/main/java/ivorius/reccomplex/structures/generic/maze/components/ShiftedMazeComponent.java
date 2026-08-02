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

import com.google.common.collect.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by lukas on 15.04.15.
 */
public class ShiftedMazeComponent<M extends MazeComponent<C>, C> implements MazeComponent<C>
{
    private final M component;
    private final MazeRoom shift;

    private final ImmutableSet<MazeRoom> rooms;
    private final ImmutableMap<MazePassage, C> exits;
    private final ImmutableMultimap<MazePassage, MazePassage> reachability;

    @Deprecated
    public ShiftedMazeComponent(M component, MazeRoom shift, ImmutableSet<MazeRoom> rooms, ImmutableMap<MazePassage, C> exits)
    {
        this.component = component;
        this.shift = shift;
        this.rooms = rooms;
        this.exits = exits;

        ImmutableSetMultimap.Builder<MazePassage, MazePassage> builder = ImmutableSetMultimap.builder();
        SetMazeComponent.connectAll(exits.keySet(), builder);
        this.reachability = builder.build();
    }

    public ShiftedMazeComponent(M component, MazeRoom shift, ImmutableSet<MazeRoom> rooms, ImmutableMap<MazePassage, C> exits, ImmutableMultimap<MazePassage, MazePassage> reachability)
    {
        this.component = component;
        this.shift = shift;
        this.rooms = rooms;
        this.exits = exits;
        this.reachability = reachability;
    }

    public M getComponent()
    {
        return component;
    }

    public MazeRoom getShift()
    {
        return shift;
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
}
