/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;

/**
 * Created by lukas on 25.02.15.
 */
public class ExpressionCache
{
    @Nonnull
    protected String expression;
    @Nullable
    protected Algebra.Expression<Boolean> parsedExpression;
    @Nullable
    protected ParseException parseException;

    public ExpressionCache(String expression)
    {
        setExpression(expression);
    }

    protected void parseExpression()
    {
        try
        {
            parsedExpression = BoolAlgebra.algebra().parse(expression);
            parseException = null;
        }
        catch (ParseException e)
        {
            parsedExpression = null;
            parseException = e;
        }
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
    public String getDisplayString()
    {
        return getExpression();
    }

    @Nullable
    public Algebra.Expression<Boolean> getParsedExpression()
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
