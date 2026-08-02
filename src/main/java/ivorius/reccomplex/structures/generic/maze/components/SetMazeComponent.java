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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by lukas on 15.04.15.
 */
public class SetMazeComponent<C> implements MorphingMazeComponent<C>
{
    public final Set<MazeRoom> rooms = new HashSet<>();
    public final Map<MazePassage, C> exits = new HashMap<>();
    public final Multimap<MazePassage, MazePassage> reachability = HashMultimap.create();

    public SetMazeComponent()
    {
    }

    @Deprecated
    public SetMazeComponent(Set<MazeRoom> rooms, Map<MazePassage, C> exits)
    {
        this(rooms, exits, HashMultimap.create());
        connectAll(exits.keySet(), reachability);
    }

    public static void connectAll(Set<MazePassage> exits, Multimap<MazePassage, MazePassage> reachability)
    {
        // Transitive, so one in both direction is enough
        List<MazePassage> connections = Lists.newArrayList(exits);
        for (int i = 1; i < connections.size(); i++)
        {
            MazePassage left = connections.get(i - 1);
            MazePassage right = connections.get(i);
            reachability.put(left, right);
            reachability.put(right, left);
        }
    }

    public static void connectAll(Set<MazePassage> exits, ImmutableMultimap.Builder<MazePassage, MazePassage> reachability)
    {
        // Transitive, so one in both direction is enough
        List<MazePassage> connections = Lists.newArrayList(exits);
        for (int i = 1; i < connections.size(); i++)
        {
            MazePassage left = connections.get(i - 1);
            MazePassage right = connections.get(i);
            reachability.put(left, right);
            reachability.put(right, left);
        }
    }

    public SetMazeComponent(Set<MazeRoom> rooms, Map<MazePassage, C> exits, Multimap<MazePassage, MazePassage> reachability)
    {
        this.rooms.addAll(rooms);
        this.exits.putAll(exits);
        this.reachability.putAll(reachability);
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

    @Override
    public void add(MazeComponent<C> component)
    {
        addReversibly(component); // One implementation, so the two can't drift apart
    }

    @Override
    public Runnable addReversibly(MazeComponent<C> component)
    {
        List<MazeRoom> addedRooms = new ArrayList<>();
        for (MazeRoom room : component.rooms())
            if (rooms.add(room))
                addedRooms.add(room);

        // Same as add: an exit the maze already had is solved by this component, so both go away
        List<MazePassage> openedExits = new ArrayList<>();
        Map<MazePassage, C> solvedExits = new HashMap<>();
        for (Map.Entry<MazePassage, C> entry : component.exits().entrySet())
        {
            C previous = exits.remove(entry.getKey());

            if (previous == null)
            {
                exits.put(entry.getKey(), entry.getValue());
                openedExits.add(entry.getKey());
            }
            else
                solvedExits.put(entry.getKey(), previous);
        }

        List<Map.Entry<MazePassage, MazePassage>> addedReachability = new ArrayList<>();
        for (Map.Entry<MazePassage, MazePassage> entry : component.reachability().entries())
            if (reachability.put(entry.getKey(), entry.getValue())) // Another component may already have it
                addedReachability.add(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue()));

        return () ->
        {
            rooms.removeAll(addedRooms);

            openedExits.forEach(exits::remove);
            exits.putAll(solvedExits);

            addedReachability.forEach(entry -> reachability.remove(entry.getKey(), entry.getValue()));
        };
    }

    @Override
    public void set(MazeComponent<C> component)
    {
        rooms.clear();
        rooms.addAll(component.rooms());

        exits.clear();
        exits.putAll(component.exits());

        reachability.clear();
        reachability.putAll(component.reachability());
    }

    @Override
    public MorphingMazeComponent<C> copy()
    {
        return new SetMazeComponent<>(rooms, exits, reachability);
    }
}
