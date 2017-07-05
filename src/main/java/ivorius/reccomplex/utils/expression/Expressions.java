/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.expression;

import net.minecraft.util.EnumFacing;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 05.07.17.
 */
public class Expressions
{
    public static PreloadedBooleanExpression<EnumFacing> direction()
    {
        return PreloadedBooleanExpression.with(exp ->
        {
            exp.addConstants(EnumFacing.values());
            exp.addEvaluators(axis -> facing -> facing.getAxis() == axis, EnumFacing.Axis.values());
            exp.addEvaluator("horizontal", f -> f.getHorizontalIndex() >= 0);
            exp.addEvaluator("vertical", f -> f.getHorizontalIndex() < 0);
        });
    }

    public static List<EnumFacing> directions(PreloadedBooleanExpression<EnumFacing> facingExpression)
    {
        return Arrays.stream(EnumFacing.values()).filter(facingExpression).collect(Collectors.toList());
    }
}
