/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.utils.ExpressionCaches;
import ivorius.reccomplex.utils.FunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import net.minecraft.util.text.TextFormatting;

import java.util.function.Predicate;

/**
 * Created by lukas on 07.09.16.
 */
public class GenerationMatcher extends FunctionExpressionCache<Boolean> implements Predicate<StructureSpawnContext>
{
    public static final String BIOME_PREFIX = "biome.";
    public static final String DIMENSION_PREFIX = "dimension.";
    public static final String DEPENDENCY_PREFIX = "structure.";

    public GenerationMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Always", expression);

        addType(new BiomeVariableType(BIOME_PREFIX, ""));
        addType(new DimensionVariableType(DIMENSION_PREFIX, ""));
        addType(new DependencyVariableType(DEPENDENCY_PREFIX, ""));
    }

    @Override
    public boolean test(StructureSpawnContext structureSpawnContext)
    {
        return evaluate(structureSpawnContext);
    }

    public class BiomeVariableType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public BiomeVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            StructureSpawnContext context = (StructureSpawnContext) args[0];
            return new BiomeMatcher(var).apply(context.world.getBiome(context.lowerCoord())); // TODO
        }

        @Override
        public boolean isKnown(String var, Object... args)
        {
            return !new BiomeMatcher(var).containsUnknownVariables();
        }
    }

    public class DimensionVariableType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public DimensionVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            StructureSpawnContext context = (StructureSpawnContext) args[0];
            return new DimensionMatcher(var).apply(context.world.provider);
        }

        @Override
        public boolean isKnown(String var, Object... args)
        {
            return !new DimensionMatcher(var).containsUnknownVariables();
        }
    }

    public class DependencyVariableType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public DependencyVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            return new DependencyMatcher(var).apply(); // TODO
        }

        @Override
        public boolean isKnown(String var, Object... args)
        {
            return !new DependencyMatcher(var).containsUnknownVariables();
        }
    }
}
