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

    public static Algebras.Unary<Boolean> not(String symbol)
    {
        return new Algebras.Unary<Boolean>(5f, Algebras.Unary.Notation.PREFIX, symbol)
        {
            @Override
            public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean> expression)
            {
                return !expression.evaluate(variableEvaluator);
            }
        };
    }

    public static Algebras.Infix<Boolean> and(String symbol)
    {
        return new Algebras.Infix<Boolean>(4f, symbol)
        {
            @Override
            public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean> left, Algebra.Expression<Boolean> right)
            {
                return left.evaluate(variableEvaluator) && right.evaluate(variableEvaluator);
            }
        };
    }

    public static Algebras.Infix<Boolean> or(String symbol)
    {
        return new Algebras.Infix<Boolean>(4f, symbol)
        {
            @Override
            public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean> left, Algebra.Expression<Boolean> right)
            {
                return left.evaluate(variableEvaluator) || right.evaluate(variableEvaluator);
            }
        };
    }

    public static Algebras.Infix<Boolean> equals(String symbol)
    {
        return new Algebras.Infix<Boolean>(3f, symbol)
        {
            @Override
            public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean> left, Algebra.Expression<Boolean> right)
            {
                return left.evaluate(variableEvaluator) == right.evaluate(variableEvaluator);
            }
        };
    }

    public static Algebras.Infix<Boolean> unEquals(String symbol)
    {
        return new Algebras.Infix<Boolean>(3f, symbol)
        {
            @Override
            public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean> left, Algebra.Expression<Boolean> right)
            {
                return left.evaluate(variableEvaluator) == right.evaluate(variableEvaluator);
            }
        };
    }

    public static Algebra.Operator<Boolean> conditional(String left, String right)
    {
        return new Algebra.Operator<Boolean>(2f, true, true, left, right)
        {
            @Override
            public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean>[] expressions)
            {
                return expressions[0].evaluate(variableEvaluator)
                        ? expressions[1].evaluate(variableEvaluator)
                        : expressions[2].evaluate(variableEvaluator);
            }
        };
    }

    public static Algebras.Parentheses parentheses(String left, String right)
    {
        return new Algebras.Parentheses(1f, left, right);
    }
}
