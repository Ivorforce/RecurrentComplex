/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.algebra;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by lukas on 26.06.16.
 */
public class ExpressionAlgebra
{
    public static <T> Algebra.Operator<Object> fromAlgebra(Algebra.Operator<T> operator, Class<T> clazz)
    {
        return new Algebra.Operator<Object>(operator.precedence, operator.hasLeftArgument, operator.hasRightArgument, operator.symbols)
        {
            @Override
            public Object evaluate(Function<String, Object> variableEvaluator, Algebra.Expression<Object>[] expressions)
            {
                Object[] result = Stream.of(expressions).map(e -> e.evaluate(variableEvaluator)).toArray();
                if (!Stream.of(result).allMatch(r -> clazz.isAssignableFrom(result.getClass())))
                    throw new IllegalArgumentException();
                return result;
            }
        };
    }
}
