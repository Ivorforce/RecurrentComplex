/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;

/**
 * Created by lukas on 23.02.15.
 */
public abstract class BoolAlgebra
{
    private static Algebra<Boolean> algebra;

    public static Algebra<Boolean> algebra()
    {
        return algebra != null ? algebra : (algebra = new Algebra<>(
                new Algebras.Closure("(", ")"),
                new Algebra.Operator<Boolean>(true, true, "|")
                {
                    @Override
                    public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean>[] expressions)
                    {
                        return expressions[0].evaluate(variableEvaluator) || expressions[1].evaluate(variableEvaluator);
                    }
                },
                new Algebra.Operator<Boolean>(true, true, "&")
                {
                    @Override
                    public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean>[] expressions)
                    {
                        return expressions[0].evaluate(variableEvaluator) && expressions[1].evaluate(variableEvaluator);
                    }
                },
                new Algebra.Operator<Boolean>(false, true, "!")
                {
                    @Override
                    public Boolean evaluate(Function<String, Boolean> variableEvaluator, Algebra.Expression<Boolean>[] expressions)
                    {
                        return !expressions[0].evaluate(variableEvaluator);
                    }
                }
        ));
    }

//    @Nullable
//    public static Predicate<Predicate<String>> tryParse(@Nonnull String string)
//    {
//        try
//        {
//            return parse(string);
//        }
//        catch (ParseException e)
//        {
//            return null;
//        }
//    }
//
//    @Nonnull
//    public static Predicate<Predicate<String>> parse(@Nonnull String string) throws ParseException
//    {
//        int index = string.length();
//
//        Predicate<Predicate<String>> expression = null;
//        int variableStart = -1;
//
//        while (--index < string.length())
//        {
//            char curChar = string.charAt(index);
//
//            switch (curChar)
//            {
//                case '(':
//                    if (variableStart >= 0 || expression != null)
//                        unexpectedCharacter(curChar, index);
//
//                    int parentheses = 1;
//                    int openIndex = index + 1;
//
//                    while (--index < string.length())
//                    {
//                        curChar = string.charAt(index);
//
//                        switch (curChar)
//                        {
//                            case '(':
//                                parentheses++;
//                                break;
//                            case ')':
//                                parentheses--;
//                                break;
//                        }
//
//                        if (parentheses == 0)
//                            break;
//                    }
//
//                    if (parentheses > 0)
//                        throw new ParseException("mismatched )", openIndex - 1);
//
//                    expression = parseInRange(string, openIndex, index - 1);
//
//                    break;
//                case '!':
//                    if (variableStart >= 0)
//                        unexpectedCharacter(curChar, index);
//
//                    expression = new Not(null);
//                    break;
//                case ' ':
//                    if (variableStart >= 0)
//                    {
//                        expression = new Variable(string.substring(variableStart, index));
//                        variableStart = -1;
//                    }
//
//                    break;
//                case '|':
//                case '&':
//                    if (variableStart >= 0)
//                    {
//                        expression = new Variable(string.substring(variableStart, index));
//                        variableStart = -1;
//                    }
//
//                    if (expression == null)
//                        throw new ParseException(String.format("binary operator '%s' missing left expression", curChar), index);
//
//                    switch (curChar)
//                    {
//                        case '|':
//                            expression = new Or(expression, parseInRange(string, index + 1, string.length()));
//                            break;
//                        case '&':
//                            expression = new And(expression, parseInRange(string, index + 1, string.length()));
//                            break;
//                    }
//
//                    break;
//                default:
//                    if (expression != null)
//                        unexpectedCharacter(curChar, index);
//
//                    if (variableStart < 0)
//                        variableStart = index;
//
//                    break;
//            }
//        }
//
//        if (variableStart >= 0)
//            expression = new Variable(string.substring(variableStart, index));
//
//        if (expression == null)
//            throw new ParseException("Expression expected", 0);
//
//        return expression;
//    }
//
//    public static Predicate<Predicate<String>> parseInRange(@Nonnull String string, int start, int end) throws ParseException
//    {
//        try
//        {
//            return parse(string.substring(start, end));
//        }
//        catch (ParseException e)
//        {
//            throw new ParseException(e.getMessage(), e.getErrorOffset() + start);
//        }
//    }
//
//    private static void unexpectedCharacter(char character, int index) throws ParseException
//    {
//        throw new ParseException(String.format("Unexpected character '%s'", character), index);
//    }
//
//    public static <T> Predicate<Predicate<T>> mapExpression(final Predicate<Predicate<String>> expression, final Function<String, T> mapping)
//    {
//        return new Predicate<Predicate<T>>()
//        {
//            @Override
//            public boolean apply(final Predicate<T> tPredicate)
//            {
//                return expression.apply(new Predicate<String>()
//                {
//                    @Override
//                    public boolean apply(String input)
//                    {
//                        return tPredicate.apply(mapping.apply(input));
//                    }
//                });
//            }
//        };
//    }
//
//    public static boolean walkVariables(Predicate<Predicate<String>> expression, Visitor<String> visitor)
//    {
//        if (expression instanceof Variable)
//            return visitor.visit(((Variable) expression).variable);
//        else if (expression instanceof Unary)
//            return walkVariables(((Unary) expression).expression, visitor);
//        else if (expression instanceof Binary)
//        {
//            return walkVariables(((Binary) expression).left, visitor)
//                    && walkVariables(((Binary) expression).right, visitor);
//        }
//
//        return true;
//    }
//
//    public static String toString(@Nonnull Predicate<Predicate<String>> expression, Function<String, String> stringMapper)
//    {
//        if (expression instanceof Variable)
//            return stringMapper.apply(((Variable) expression).variable);
//        else if (expression instanceof Unary)
//        {
//            String subExpressionString = subToString(((Unary) expression).expression, stringMapper);
//
//            if (expression instanceof Not)
//                return "!" + subExpressionString;
//        }
//        else if (expression instanceof Binary)
//        {
//            String leftString = subToString(((Binary) expression).left, stringMapper);
//            String rightString = subToString(((Binary) expression).right, stringMapper);
//
//            if (expression instanceof And)
//                return String.format("%s & %s", leftString, rightString);
//            else if (expression instanceof Or)
//                return String.format("%s | %s", leftString, rightString);
//        }
//
//        throw new IllegalArgumentException();
//    }
//
//    protected static String subToString(Predicate<Predicate<String>> subExpression, Function<String, String> stringMapper)
//    {
//        return subExpression instanceof Unary || subExpression instanceof Variable
//                ? toString(subExpression, stringMapper)
//                : String.format("(%s)", toString(subExpression, stringMapper));
//    }
//
//    public abstract static class Unary implements Predicate<Predicate<String>>
//    {
//        public Predicate<Predicate<String>> expression;
//
//        public Unary(Predicate<Predicate<String>> expression)
//        {
//            this.expression = expression;
//        }
//    }
//
//    public abstract static class Binary implements Predicate<Predicate<String>>
//    {
//        public Predicate<Predicate<String>> left;
//        public Predicate<Predicate<String>> right;
//
//        public Binary(Predicate<Predicate<String>> left, Predicate<Predicate<String>> right)
//        {
//            this.left = left;
//            this.right = right;
//        }
//    }
//
//    public static class Variable implements Predicate<Predicate<String>>
//    {
//        public String variable;
//
//        public Variable(String variable)
//        {
//            this.variable = variable;
//        }
//
//        @Override
//        public boolean apply(Predicate<String> input)
//        {
//            return input != null && input.apply(variable);
//        }
//    }
//
//    public static class Not extends Unary
//    {
//        public Not(Predicate<Predicate<String>> expression)
//        {
//            super(expression);
//        }
//
//        @Override
//        public boolean apply(Predicate<String> input)
//        {
//            return !expression.apply(input);
//        }
//    }
//
//    public static class And extends Binary
//    {
//        public And(Predicate<Predicate<String>> left, Predicate<Predicate<String>> right)
//        {
//            super(left, right);
//        }
//
//        @Override
//        public boolean apply(Predicate<String> input)
//        {
//            return left.apply(input) && right.apply(input);
//        }
//    }
//
//    public static class Or extends Binary
//    {
//        public Or(Predicate<Predicate<String>> left, Predicate<Predicate<String>> right)
//        {
//            super(left, right);
//        }
//
//        @Override
//        public boolean apply(Predicate<String> input)
//        {
//            return left.apply(input) || right.apply(input);
//        }
//    }
}
