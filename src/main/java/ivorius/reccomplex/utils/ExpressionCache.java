/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.reccomplex.utils.algebra.Algebra;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;

/**
 * Created by lukas on 25.02.15.
 */
public class ExpressionCache<T>
{
    @Nonnull
    protected Algebra<T> algebra;
    protected T emptyExpressionResult;
    protected String emptyResultRepresentation;

    @Nonnull
    protected String expression = "";
    @Nullable
    protected Algebra.Expression<T> parsedExpression;
    @Nullable
    protected ParseException parseException;

    public ExpressionCache(@Nonnull Algebra<T> algebra, @Nonnull String expression)
    {
        this.algebra = algebra;
        setExpression(expression);
    }

    public ExpressionCache(@Nonnull Algebra<T> algebra, T emptyExpressionResult, String emptyResultRepresentation, @Nonnull String expression)
    {
        this.algebra = algebra;
        this.emptyExpressionResult = emptyExpressionResult;
        this.emptyResultRepresentation = emptyResultRepresentation;
        setExpression(expression);
    }

    protected void parseExpression()
    {
        if (expressionIsEmpty() && acceptsEmptyExpression())
        {
            parsedExpression = new Algebra.Value<>(0, emptyExpressionResult, emptyResultRepresentation);
            parseException = null;
        }
        else
        {
            try
            {
                parsedExpression = algebra.parse(expression);
                parseException = null;

                testVariables();
            }
            catch (ParseException e)
            {
                parsedExpression = null;
                parseException = e;
            }
        }
    }

    protected void testVariables()
    {
        if (parsedExpression != null)
        {
            try
            {
                testVariables(parsedExpression);
            }
            catch (ParseException e)
            {
                parsedExpression = null;
                parseException = e;
            }
        }
    }

    protected void testVariables(@Nonnull Algebra.Expression<T> expression) throws ParseException
    {

    }

    @Nonnull
    public String getExpression()
    {
        return expression;
    }

    public void setExpression(@Nonnull String expression)
    {
        this.expression = expression;
        parseExpression();
    }

    @Nonnull
    public Algebra<T> getAlgebra()
    {
        return algebra;
    }

    public void setAlgebra(@Nonnull Algebra<T> algebra)
    {
        this.algebra = algebra;
    }

    public T getEmptyExpressionResult()
    {
        return emptyExpressionResult;
    }

    public String getEmptyExpressionResultRepresentation()
    {
        return emptyResultRepresentation;
    }

    public void setNoEmptyExpressionResult()
    {
        this.emptyExpressionResult = null;
        this.emptyResultRepresentation = null;
        parseExpression();
    }

    public void setEmptyExpressionResult(T emptyResult, String representation)
    {
        this.emptyExpressionResult = emptyResult;
        this.emptyResultRepresentation = representation;
        parseExpression();
    }

    public boolean acceptsEmptyExpression()
    {
        return emptyResultRepresentation != null;
    }

    public boolean expressionIsEmpty()
    {
        return expression.trim().isEmpty();
    }

    @Nullable
    public Algebra.Expression<T> getParsedExpression()
    {
        return parsedExpression;
    }

    @Nullable
    public ParseException getParseException()
    {
        return parseException;
    }

    public boolean isExpressionValid()
    {
        return parsedExpression != null && parseException == null;
    }
}
