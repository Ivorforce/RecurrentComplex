/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.text.ParseException;

/**
 * Created by lukas on 24.02.15.
 */
public class Algebras
{
    @Nullable
    public static <T> T tryEvaluate(String expression, Algebra<T> algebra, Function<String, T> variableEvaluator)
    {
        Algebra.Expression<T> parsed = tryParse(expression, algebra);
        return parsed != null ? parsed.evaluate(variableEvaluator) : null;
    }

    @Nullable
    public static <T> Algebra.Expression<T> tryParse(String string, Algebra<T> algebra)
    {
        try
        {
            return algebra.parse(string);
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    public static class Parentheses<T> extends Algebra.Operator<T>
    {
        public Parentheses(float precedence, String open, String close)
        {
            super(precedence, false, false, open, close);
        }

        @Override
        public T evaluate(Function<String, T> variableEvaluator, Algebra.Expression<T>[] expressions)
        {
            return expressions[0].evaluate(variableEvaluator);
        }
    }

    public static abstract class Unary<T> extends Algebra.Operator<T>
    {
        public enum Notation
        {
            PREFIX, POSTFIX
        }

        public Unary(float precedence, Notation notation, String symbol)
        {
            super(precedence, notation == Notation.POSTFIX, notation == Notation.PREFIX, symbol);
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
        public Infix(float precedence, String symbol)
        {
            super(precedence, true, true, symbol);
        }

        @Override
        public T evaluate(Function<String, T> variableEvaluator, Algebra.Expression<T>[] expressions)
        {
            return evaluate(variableEvaluator, expressions[0], expressions[1]);
        }

        public abstract T evaluate(Function<String, T> variableEvaluator, Algebra.Expression<T> left, Algebra.Expression<T> right);
    }
}
