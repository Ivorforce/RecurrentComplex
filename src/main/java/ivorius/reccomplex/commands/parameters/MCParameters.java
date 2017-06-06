/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Created by lukas on 31.05.17.
 */
public class MCParameters extends Parameters
{
    public MCParameters(Parameters blueprint)
    {
        super(blueprint);
    }

    public static MCParameters of(String[] args, Function<Parameters, Parameters> c)
    {
        return new MCParameters(Parameters.of(args, c));
    }

    @Override
    public MCParameter get()
    {
        return new MCParameter(super.get());
    }

    @Override
    public MCParameter get(int idx)
    {
        return new MCParameter(super.get(idx));
    }

    @Override
    public MCParameter get(@Nonnull String name)
    {
        return new MCParameter(super.get(name));
    }

    public Parameter.Result<BlockPos> pos(String x, String y, String z, BlockPos ref, boolean centerBlock)
    {
        return get(x).pos(this.get(y), this.get(z), ref, centerBlock);
    }
}
