/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.base.Function;

import javax.annotation.Nullable;

/**
 * Created by lukas on 23.02.15.
 */
public abstract class BoolAlgebra
{
    private static Algebra<Boolean> algebra;

    public static Algebra<Boolean> newAlgebra()
    {
        return new Algebra<>(
                new Algebras.Closure(1f, "(", ")"),
                new Algebras.Infix<Boolean>(2f, "|")
                {
                    @Override
                    public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean> left, Algebra.Expression<Boolean> right)
                    {
                        return left.evaluate(variableEvaluator) || right.evaluate(variableEvaluator);
                    }
                },
                new Algebras.Infix<Boolean>(2f, "&")
                {
                    @Override
                    public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean> left, Algebra.Expression<Boolean> right)
                    {
                        return left.evaluate(variableEvaluator) && right.evaluate(variableEvaluator);
                    }
                },
                new Algebras.Unary<Boolean>(3f, Algebras.Unary.Notation.PREFIX, "!")
                {
                    @Override
                    public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean> expression)
                    {
                        return !expression.evaluate(variableEvaluator);
                    }
                }
        );
    }

    public static Algebra<Boolean> algebra()
    {
        return algebra != null ? algebra : (algebra = newAlgebra());
    }

    public static Function<String, Boolean> constantEvaluator()
    {
        return new Function<String, Boolean>()
        {
            @Nullable
            @Override
            public Boolean apply(String input)
            {
                return Boolean.valueOf(input);
            }
        };
    }
}
