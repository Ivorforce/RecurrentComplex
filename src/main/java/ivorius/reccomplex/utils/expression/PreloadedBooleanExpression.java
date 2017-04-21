/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.expression;

import ivorius.reccomplex.utils.algebra.BoolFunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import ivorius.reccomplex.utils.algebra.SupplierCache;

import java.text.ParseException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by lukas on 21.04.17.
 */
public class PreloadedBooleanExpression<A> extends BoolFunctionExpressionCache<A, Object>
{
    public PreloadedBooleanExpression()
    {
        this(true, "Any");
    }

    public PreloadedBooleanExpression(Boolean emptyResult, String emptyResultRepresentation)
    {
        super(RCBoolAlgebra.algebra(), emptyResult, emptyResultRepresentation);
    }

    public static <A> PreloadedBooleanExpression<A> with(Consumer<PreloadedBooleanExpression<A>> consumer)
    {
        PreloadedBooleanExpression<A> exp = new PreloadedBooleanExpression<A>();
        consumer.accept(exp);
        return exp;
    }

    @SafeVarargs
    public final void addConstant(String id, A... as)
    {
        addEvaluator(id, a -> Arrays.stream(as).anyMatch(a::equals));
    }

    @SafeVarargs
    public final void addConstants(A... as)
    {
        for (A a : as)
            addConstant(a.toString(), a);
    }

    public <B> void addEvaluators(Function<B, Predicate<A>> predicate, B... bs)
    {
        for (B b : bs)
            addEvaluator(b.toString(), predicate.apply(b));
    }

    public void addEvaluator(String id, Predicate<A> predicate)
    {
        addType(new VariableType<Boolean, A, Object>(id, "")
        {
            @Override
            public Function<SupplierCache<A>, Boolean> parse(String var) throws ParseException
            {
                return a -> predicate.test(a.get());
            }

            @Override
            public Validity validity(String var, Object o)
            {
                return Validity.KNOWN;
            }
        });
    }
}
