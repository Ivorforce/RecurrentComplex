/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.base.Function;

/**
 * Created by lukas on 24.02.15.
 */
public class Algebras
{
    public static class Closure<T> extends Algebra.Operator<T>
    {
        public Closure(String open, String close)
        {
            super(false, false, open, close);
        }

        @Override
        public T evaluate(Function<String, T> variableEvaluator, Algebra.Expression<T>[] expressions)
        {
            return expressions[0].evaluate(variableEvaluator);
        }
    }

    public static abstract class Unary<T> extends Algebra.Operator<T>
    {
        public static enum Notation
        {
            PREFIX, POSTFIX
        }

        public Unary(Notation notation, String symbol)
        {
            super(notation == Notation.POSTFIX, notation == Notation.PREFIX, symbol);
        }

        @Override
        public T evaluate(Function<String, T> variableEvaluator, Algebra.Expression<T>[] expressions)
        {
            return evaluate(variableEvaluator, expressions[0]);
        }

        public abstract T evaluate(Function<String, T> variableEvaluator, Algebra.Expression<T> expression);
    }

    public static abstract class Infix<T> extends Algebra.Operator<T>
    {
        public Infix(String symbol)
        {
            super(true, true, symbol);
        }

        @Override
        public T evaluate(Function<String, T> variableEvaluator, Algebra.Expression<T>[] expressions)
        {
            return evaluate(variableEvaluator, expressions[0], expressions[1]);
        }

        public abstract T evaluate(Function<String, T> variableEvaluator, Algebra.Expression<T> left, Algebra.Expression<T> right);
    }
}
