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
import java.util.function.Consumer;

/**
 * Created by lukas on 31.05.17.
 */
public class IvParameters extends MCParameters
{
    public IvParameters(Parameters blueprint)
    {
        super(blueprint);
    }

    public static IvParameters of(String[] args, Consumer<Parameters> c)
    {
        return new IvParameters(Parameters.of(args, c));
    }

    public IvParameter iv()
    {
        return new IvParameter(get());
    }

    public IvParameter iv(@Nonnull String name)
    {
        return new IvParameter(get(name));
    }

    public Parameter.Result<BlockSurfacePos> surfacePos(String x, String z, BlockPos ref, boolean centerBlock)
    {
        return this.iv(x).surfacePos(iv(z), ref, centerBlock);
    }

    public Parameter.Result<AxisAlignedTransform2D> transform(String rotation, String mirror) throws CommandException
    {
        return iv(rotation).transform(has(mirror));
    }

}
