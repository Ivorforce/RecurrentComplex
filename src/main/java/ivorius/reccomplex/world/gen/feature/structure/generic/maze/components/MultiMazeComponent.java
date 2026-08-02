/*
 * Copyright 2017 Lukas Tenbrink
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

package ivorius.reccomplex.world.gen.feature.structure.generic.maze.components;

import com.google.common.collect.Multimap;
import ivorius.ivtoolkit.random.WeightedSelector;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by lukas on 14.04.17.
 */
public class MultiMazeComponent<M extends WeightedMazeComponent<C>, C> implements WeightedMazeComponent<C>
{
    private final List<M> components;
    private final double weight;

    public MultiMazeComponent(List<M> components)
    {
        this.components = components;
        weight = components.stream().mapToDouble(WeightedMazeComponent::getWeight).sum();
    }

    public M first()
    {
        return components.get(0);
    }

    @Override
    public Set<MazeRoom> rooms()
    {
        return first().rooms();
    }

    @Override
    public Map<MazePassage, C> exits()
    {
        return first().exits();
    }

    @Override
    public Multimap<MazePassage, MazePassage> reachability()
    {
        return first().reachability();
    }

    @Override
    public double getWeight()
    {
        return weight;
    }

    public PlacedMazeComponent<M, C> place(MazeRoom shift, Random random)
    {
        return new PlacedMazeComponent<M, C>(WeightedSelector.selectItem(random, components), shift);
    }
}
