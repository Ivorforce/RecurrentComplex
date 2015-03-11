/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.base.Function;

/**
 * Created by lukas on 23.02.15.
 */
public abstract class BoolAlgebra
{
    private static Algebra<Boolean> algebra;

    public static Algebra<Boolean> newAlgebra()
    {
        return new Algebra<>(
                new Algebras.Closure("(", ")"),
                new Algebras.Infix<Boolean>("|")
                {
                    @Override
                    public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean> left, Algebra.Expression<Boolean> right)
                    {
                        return left.evaluate(variableEvaluator) || right.evaluate(variableEvaluator);
                    }
                },
                new Algebras.Infix<Boolean>("&")
                {
                    @Override
                    public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean> left, Algebra.Expression<Boolean> right)
                    {
                        return left.evaluate(variableEvaluator) && right.evaluate(variableEvaluator);
                    }
                },
                new Algebras.Unary<Boolean>(Algebras.Unary.Notation.PREFIX, "!")
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
}
