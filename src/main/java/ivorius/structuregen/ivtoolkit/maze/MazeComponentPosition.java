/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.structuregen.ivtoolkit.maze;

import net.minecraft.util.WeightedRandom;

/**
 * Created by lukas on 20.06.14.
 */
public class MazeComponentPosition extends WeightedRandom.Item
{
    private MazeComponent component;

    private MazeRoom positionInMaze;

    public MazeComponentPosition(MazeComponent component, MazeRoom positionInMaze)
    {
        super(component.itemWeight);

        this.component = component;
        this.positionInMaze = positionInMaze;
    }

    public MazeComponent getComponent()
    {
        return component;
    }

    public MazeRoom getPositionInMaze()
    {
        return positionInMaze;
    }
}
