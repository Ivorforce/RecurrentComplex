/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import net.minecraft.command.CommandBase;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * Created by lukas on 31.05.17.
 */
public class IvExpect<T extends IvExpect<T>> extends MCExpect<T>
{
    IvExpect()
    {

    }

    public static <T extends IvExpect<T>> T startIV()
    {
        //noinspection unchecked
        return (T) new IvExpect();
    }

    public T surfacePos(@Nullable BlockPos pos)
    {
        return next(args -> CommandBase.getTabCompletionCoordinateXZ(args, 0, pos))
                .next(args -> CommandBase.getTabCompletionCoordinateXZ(args, 1, pos));
    }
}
