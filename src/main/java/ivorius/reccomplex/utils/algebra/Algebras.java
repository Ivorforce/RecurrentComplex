/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.algebra;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.function.Function;

/**
 * Created by lukas on 24.02.15.
 */
public class Algebras
{
    @Nullable
    public static <T> T tryEvaluate(String expression, Algebra<T> algebra, Function<String, T> variableEvaluator)
    {
        Algebra.Expression<T, T> parsed = tryParse(expression, algebra, variableEvaluator::apply);
        return parsed != null ? parsed.evaluate(t -> t) : null;
    }

    @Nullable
    public static <T, V> Algebra.Expression<T, V> tryParse(String string, Algebra<T> algebra, Algebra.VariableParser<V> variableParser)
    {
        try
        {
            return algebra.parse(string, variableParser);
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
        public <V> T evaluate(Function<V, T> variableEvaluator, Algebra.Expression<T, V>[] expressions)
        {
            return expressions[0].evaluate(variableEvaluator);
        }
    }

    public static abstract class Unary<T> extends Algebra.Operator<T>
    {
        public Unary(float precedence, Notation notation, String symbol)
        {
            super(precedence, notation == Notation.POSTFIX, notation == Notation.PREFIX, symbol);
        }

        @Override
        public <V> T evaluate(Function<V, T> variableEvaluator, Algebra.Expression<T, V>[] expressions)
        {
            return evaluate(variableEvaluator, expressions[0]);
        }

        public abstract <V> T evaluate(Function<V, T> variableEvaluator, Algebra.Expression<T, V> expression);

        public enum Notation
        {
            PREFIX, POSTFIX
        }
    }

    public static abstract class Infix<T> extends Algebra.Operator<T>
    {
        public Infix(float precedence, String symbol)
        {
            super(precedence, true, true, symbol);
        }

        @Override
        public <V> T evaluate(Function<V, T> variableEvaluator, Algebra.Expression<T, V>[] expressions)
        {
            return evaluate(variableEvaluator, expressions[0], expressions[1]);
        }

        public abstract <V> T evaluate(Function<V, T> variableEvaluator, Algebra.Expression<T, V> left, Algebra.Expression<T, V> right);
    }
}
