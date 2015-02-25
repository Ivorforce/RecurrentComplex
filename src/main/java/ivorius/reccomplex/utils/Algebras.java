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
}
