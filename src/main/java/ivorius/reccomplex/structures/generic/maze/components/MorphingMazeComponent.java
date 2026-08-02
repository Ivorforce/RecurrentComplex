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
 * Created by lukas on 15.04.15.
 */
public interface MorphingMazeComponent<C> extends MazeComponent<C>
{
    void add(MazeComponent<C> component);

    /**
     * Adds the component, returning a task that undoes exactly this add.
     * <p>
     * Only valid while this is the most recent add still applied - callers must undo in reverse
     * order. The default snapshots the whole maze; implementations that can track their own changes
     * should override, since a maze search adds and undoes once per placement.
     */
    default Runnable addReversibly(MazeComponent<C> component)
    {
        MorphingMazeComponent<C> before = copy();
        add(component);
        return () -> set(before);
    }

    void set(MazeComponent<C> component);

    MorphingMazeComponent<C> copy();
}
