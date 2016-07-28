/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.algebra;

import java.util.function.Function;

/**
 * Created by lukas on 26.06.16.
 */
public class IntAlgebra
{
    public static final Function<String, Integer> CONSTANT_EVALUATOR = Integer::valueOf;

    public static Algebras.Infix<Integer> plus(String symbol)
    {
        return new Algebras.Infix<Integer>(4f, symbol)
        {
            @Override
            public Integer evaluate(Function<String, Integer> variableEvaluator, Algebra.Expression<Integer> left, Algebra.Expression<Integer> right)
            {
                return left.evaluate(variableEvaluator) + right.evaluate(variableEvaluator);
            }
        };
    }

    public static Algebras.Infix<Integer> minus(String symbol)
    {
        return new Algebras.Infix<Integer>(4f, symbol)
        {
            @Override
            public Integer evaluate(Function<String, Integer> variableEvaluator, Algebra.Expression<Integer> left, Algebra.Expression<Integer> right)
            {
                return left.evaluate(variableEvaluator) - right.evaluate(variableEvaluator);
            }
        };
    }

    public static Algebras.Parentheses<Integer> parentheses(String left, String right)
    {
        return new Algebras.Parentheses<>(1f, left, right);
    }
}
