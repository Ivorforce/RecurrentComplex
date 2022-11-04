/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.client.rendering;

import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class MazeVisualizationContext
{
    protected BlockPos lowerCoord;
    protected int[] scale;

    public MazeVisualizationContext(BlockPos lowerCoord, int[] scale)
    {
        this.lowerCoord = lowerCoord;
        this.scale = scale != null ? scale : new int[]{1, 1, 1};
    }

    @Nonnull
    public Selection mapSelection(Selection selection)
    {
        Selection realWorldSelection = new Selection(3);
        for (Selection.Area area : selection)
        {
            BlockPos min = min(new MazeRoom(area.getMinCoord()));
            BlockPos max = max(new MazeRoom(area.getMaxCoord()));

            realWorldSelection.add(Selection.Area.from(area.isAdditive(),
                    BlockPositions.toIntArray(min), BlockPositions.toIntArray(max),
                    area.getIdentifier()));
        }
        return realWorldSelection;
    }

    @Nonnull
    protected BlockPos apply(int[] coordinates)
    {
        return lowerCoord.add(BlockPositions.fromIntArray(IvVecMathHelper.mul(scale, coordinates)));
    }

    public BlockPos min(MazeRoom room)
    {
        return apply(room.getCoordinates());
    }

    public BlockPos max(MazeRoom room)
    {
        int[] one = new int[room.getDimensions()];
        Arrays.fill(one, 1);

        return apply(IvVecMathHelper.add(room.getCoordinates(), one))
                .subtract(new Vec3i(1, 1, 1));
    }
}
