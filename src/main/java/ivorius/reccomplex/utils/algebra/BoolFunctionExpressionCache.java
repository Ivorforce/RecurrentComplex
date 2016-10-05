/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.algebra;

import java.util.function.Predicate;

/**
 * Created by lukas on 05.10.16.
 */
public class BoolFunctionExpressionCache<A, U> extends FunctionExpressionCache<Boolean, A, U> implements Predicate<A>
{
    public BoolFunctionExpressionCache(Algebra<Boolean> algebra, String expression)
    {
        super(algebra, expression);

        addBoolConstants();
    }

    public BoolFunctionExpressionCache(Algebra<Boolean> algebra, Boolean emptyResult, String emptyResultRepresentation, String expression)
    {
        super(algebra, emptyResult, emptyResultRepresentation, expression);

        addBoolConstants();
    }

    protected void addBoolConstants()
    {
        addType(FunctionExpressionCaches.constant("true", true));
        addType(FunctionExpressionCaches.constant("false", false));
    }

    @Override
    public boolean test(A a)
    {
        return evaluate(a);
    }

    public static class VariableTypeConstant
    {

    }
}
