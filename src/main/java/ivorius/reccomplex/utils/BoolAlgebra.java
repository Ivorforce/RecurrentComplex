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
    public static final Function<String, Boolean> CONSTANT_EVALUATOR = new Function<String, Boolean>()
    {
        @Nullable
        @Override
        public Boolean apply(String input)
        {
            return Boolean.valueOf(input);
        }
    };

    public static Algebras.Unary<Boolean> not()
    {
        return new Algebras.Unary<Boolean>(3f, Algebras.Unary.Notation.PREFIX, "!")
        {
            @Override
            public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean> expression)
            {
                return !expression.evaluate(variableEvaluator);
            }
        };
    }

    public static Algebras.Infix<Boolean> and()
    {
        return new Algebras.Infix<Boolean>(2f, "&")
        {
            @Override
            public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean> left, Algebra.Expression<Boolean> right)
            {
                return left.evaluate(variableEvaluator) && right.evaluate(variableEvaluator);
            }
        };
    }

    public static Algebras.Infix<Boolean> or()
    {
        return new Algebras.Infix<Boolean>(2f, "|")
        {
            @Override
            public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean> left, Algebra.Expression<Boolean> right)
            {
                return left.evaluate(variableEvaluator) || right.evaluate(variableEvaluator);
            }
        };
    }

    public static Algebras.Closure closure()
    {
        return new Algebras.Closure(1f, "(", ")");
    }
}
