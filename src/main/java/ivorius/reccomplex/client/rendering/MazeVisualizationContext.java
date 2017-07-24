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
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Function;

public class MazeVisualizationContext
{
    protected Function<MazeRoom, BlockPos> mapper;

    public MazeVisualizationContext(Function<MazeRoom, BlockPos> mapper)
    {
        this.mapper = mapper;
    }

    @NotNull
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

    public BlockPos min(MazeRoom room)
    {
        return mapper.apply(room);
    }

    public BlockPos max(MazeRoom room)
    {
        int[] one = new int[room.getDimensions()];
        Arrays.fill(one, 1);

        return mapper.apply(new MazeRoom(IvVecMathHelper.add(room.getCoordinates(), one)))
                .subtract(new Vec3i(1, 1, 1));
    }
}
