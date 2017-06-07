/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.function.BinaryOperator;

/**
 * Created by lukas on 31.05.17.
 */
public class IvParameter<P extends IvParameter<P>> extends MCParameter<P>
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
    public P copy(Parameter<String, ?> p)
    {
        //noinspection unchecked
        return (P) new IvParameter<>(p);
    }

    @Nonnull
    public Parameter<BlockSurfacePos, ?> surfacePos(Parameter<String, ?> zp, BlockPos ref, boolean centerBlock)
    {
        return orElse("~").flatMap(x ->
                zp.orElse("~").map(z ->
                        parseSurfacePos(ref, x, z, centerBlock)
                ));
    }

    public Parameter<BlockSurfacePos, ?> surfacePos(BlockPos ref, boolean centerBlock)
    {
        return surfacePos(move(1), ref, centerBlock);
    }

    public Parameter<AxisAlignedTransform2D, ?> transform(boolean mirror) throws CommandException
    {
        if (has(1) || mirror)
            return map(CommandBase::parseInt)
                    .map(i -> i > 40 ? i / 90 : i)
                    .orElse(0).map(r -> AxisAlignedTransform2D.from(r, mirror));
        return new Parameter<>(this, s -> null);
    }
}
