/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import net.minecraft.command.CommandException;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Created by lukas on 31.05.17.
 */
public class IvParameters extends MCParameters
{
    public IvParameters(Parameters blueprint)
    {
        super(blueprint);
    }

    public static IvParameters of(String[] args, Function<Parameters, Parameters> c)
    {
        return new IvParameters(Parameters.of(args, c));
    }

    @Override
    public IvParameter<?> get(int idx)
    {
        return new IvParameter(super.get(idx));
    }

    @Override
    public IvParameter<?> get(@Nonnull String name)
    {
        return new IvParameter(super.get(name));
    }

    public Parameter<BlockSurfacePos, ?> surfacePos(String x, String z, BlockPos ref, boolean centerBlock)
    {
        return get(x).surfacePos(get(z), ref, centerBlock);
    }

    public Parameter<AxisAlignedTransform2D, ?> transform(String rotation, String mirror) throws CommandException
    {
        return get(rotation).transform(has(mirror));
    }

}
