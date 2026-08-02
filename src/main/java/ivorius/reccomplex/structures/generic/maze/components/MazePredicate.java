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

/**
 * Created by lukas on 30.09.15.
 */
public interface MazePredicate<M extends MazeComponent<C>, C>
{
    boolean canPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component);

    void willPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component);

    void didPlace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component);

    void willUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component);

    void didUnplace(MorphingMazeComponent<C> maze, ShiftedMazeComponent<M, C> component);

    boolean isDirtyConnection(MazeRoom dest, MazeRoom source, C c);
}
