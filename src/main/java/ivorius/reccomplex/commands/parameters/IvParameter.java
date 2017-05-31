/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import net.minecraft.command.CommandBase;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 31.05.17.
 */
public class IvParameter extends Parameter
{
    public IvParameter(Parameter other)
    {
        super(other);
    }

    // Since CommandBase's version requires a sender
    public static BlockSurfacePos parseSurfacePos(BlockPos blockpos, String x, String z, boolean centerBlock) throws NumberInvalidException
    {
        return BlockSurfacePos.from(new BlockPos(CommandBase.parseDouble((double) blockpos.getX(), x, -30000000, 30000000, centerBlock), 0, CommandBase.parseDouble((double) blockpos.getZ(), z, -30000000, 30000000, centerBlock)));
    }

    @Override
    public IvParameter move(int idx)
    {
        return new IvParameter(super.move(idx));
    }

    @Nonnull
    public Parameter.Result<BlockSurfacePos> surfacePos(BlockPos ref, boolean centerBlock)
    {
        return first().failable().flatMap(x -> at(1).map(z ->
                parseSurfacePos(ref, x, z, centerBlock)))
                .orElse(() -> BlockSurfacePos.from(ref));
    }
}
