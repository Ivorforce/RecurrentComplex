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

/**
 * Created by lukas on 14.04.17.
 */
public class PlacedMazeComponent<M extends WeightedMazeComponent<C>, C>
{
    private final M component;
    private final MazeRoom shift;

    public PlacedMazeComponent(M component, MazeRoom shift)
    {
        this.component = component;
        this.shift = shift;
    }

    public M component()
    {
        return component;
    }

    public MazeRoom shift()
    {
        return shift;
    }
}
