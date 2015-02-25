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
}
