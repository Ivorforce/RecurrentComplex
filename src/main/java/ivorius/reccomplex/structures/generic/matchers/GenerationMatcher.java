/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.utils.FunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by lukas on 07.09.16.
 */
public class GenerationMatcher extends FunctionExpressionCache<Boolean, GenerationMatcher.Argument, Object> implements Predicate<GenerationMatcher.Argument>
{
    public static final String BIOME_PREFIX = "biome.";
    public static final String DIMENSION_PREFIX = "dimension.";
    public static final String DEPENDENCY_PREFIX = "dependency.";

    public GenerationMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Always", expression);

        addType(new BiomeVariableType(BIOME_PREFIX, ""));
        addTypes(new DimensionVariableType(DIMENSION_PREFIX, ""), t -> t.alias("dim.", ""));
        addTypes(new DependencyVariableType(DEPENDENCY_PREFIX, ""), t -> t.alias("dep.", ""));

        testVariables();
    }

    @Override
    public boolean test(Argument argument)
    {
        return evaluate(argument);
    }

    public static class Argument
    {
        public final StructurePrepareContext context;
        public final Biome biome;

        public Argument(StructurePrepareContext context, Biome biome)
        {
            this.context = context;
            this.biome = biome;
        }
    }

    public static class BiomeVariableType extends DelegatingVariableType<Boolean, Argument, Object, Biome, Set<Biome>, BiomeMatcher>
    {
        public BiomeVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Biome convertEvaluateArgument(Argument argument)
        {
            return argument.biome;
        }

        @Override
        public Set<Biome> convertIsKnownArgument(Object object)
        {
            return BiomeMatcher.gatherAllBiomes();
        }

        @Override
        public BiomeMatcher createCache(String var)
        {
            return new BiomeMatcher(var);
        }
    }

    public static class DimensionVariableType extends DelegatingVariableType<Boolean, Argument, Object, WorldProvider, Object, DimensionMatcher>
    {
        public DimensionVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public WorldProvider convertEvaluateArgument(Argument argument)
        {
            return argument.context.environment.world.provider;
        }

        @Override
        public DimensionMatcher createCache(String var)
        {
            return new DimensionMatcher(var);
        }
    }

    public static class DependencyVariableType extends DelegatingVariableType<Boolean, Argument, Object, Object, Object, DependencyMatcher>
    {
        public DependencyVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public DependencyMatcher createCache(String var)
        {
            return new DependencyMatcher(var);
        }
    }
}
