/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.rcparameters;

import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.commands.parameters.Parameter;
import ivorius.reccomplex.commands.parameters.Parameters;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Created by lukas on 31.05.17.
 */
public class IvP
{
    // Since CommandBase's version requires a sender
    public static BlockSurfacePos parseSurfacePos(BlockPos blockpos, String x, String z, boolean centerBlock) throws NumberInvalidException
    {
        return BlockSurfacePos.from(new BlockPos(CommandBase.parseDouble((double) blockpos.getX(), x, -30000000, 30000000, centerBlock), 0, CommandBase.parseDouble((double) blockpos.getZ(), z, -30000000, 30000000, centerBlock)));
    }

    @Nonnull
    public static Function<Parameter<String>, Parameter<BlockSurfacePos>> surfacePos(Parameter<String> zp, BlockPos ref, boolean centerBlock)
    {
        return p -> p.orElse("~").flatMap(x ->
                zp.orElse("~").map(z ->
                        parseSurfacePos(ref, x, z, centerBlock)
                ));
    }

    public static Function<Parameter<String>, Parameter<BlockSurfacePos>> surfacePos(BlockPos ref, boolean centerBlock)
    {
        return p -> surfacePos(p.move(1), ref, centerBlock).apply(p);
    }

    public static Function<Parameter<String>, Parameter<AxisAlignedTransform2D>> transform(boolean mirror)
    {
        return p ->
        {
            if (p.has(1) || mirror)
                return p.map(CommandBase::parseInt)
                        .map(i -> i > 40 ? i / 90 : i)
                        .orElse(0).map(r -> AxisAlignedTransform2D.from(r, mirror));
            return new Parameter<>(p, s -> null);
        };
    }

    public static Function<Parameters, Parameter<BlockSurfacePos>> surfacePos(String x, String z, BlockPos ref, boolean centerBlock)
    {
        return ps -> ps.get(x).to(surfacePos(ps.get(z), ref, centerBlock));
    }

    public static Function<Parameters, Parameter<AxisAlignedTransform2D>> transform(String rotation, String mirror) throws CommandException
    {
        return ps -> ps.get(rotation).to(transform(ps.has(mirror)));
    }
}
