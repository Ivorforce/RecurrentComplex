/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.expression;

import ivorius.reccomplex.utils.algebra.FunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.SupplierCache;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
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

    public static <T> FunctionExpressionCache.VariableType<Boolean, T, Object> dictionaryVariableType(String prefix, String suffix, Function<String, T> converter, Predicate<String> known)
    {
        return new FunctionExpressionCache.VariableType<Boolean, T, Object>(prefix, suffix)
        {
            @Override
            public Function<SupplierCache<T>, Boolean> parse(String var) throws ParseException
            {
                T t = converter.apply(var);
                return t != null ? v -> v.get() == t : b -> false;
            }

            @Override
            public FunctionExpressionCache.Validity validity(String var, Object o)
            {
                return known.test(var) ? FunctionExpressionCache.Validity.KNOWN : FunctionExpressionCache.Validity.UNKNOWN;
            }
        };
    }

    public static <T> FunctionExpressionCache.VariableType<Boolean, T, Object> registryVariableType(String prefix, String suffix, RegistryNamespaced<ResourceLocation, T> registry)
    {
        return dictionaryVariableType(prefix, suffix, s -> registry.getObject(new ResourceLocation(s)), s -> registry.containsKey(new ResourceLocation(s)));
    }
}
