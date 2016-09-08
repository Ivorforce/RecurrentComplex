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
public class GenerationMatcher extends FunctionExpressionCache<Boolean, GenerationMatcher.Argument, StructurePrepareContext> implements Predicate<GenerationMatcher.Argument>
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

    public static class BiomeVariableType extends DelegatingVariableType<Boolean, Argument, StructurePrepareContext, Biome, Set<Biome>, BiomeMatcher>
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
        public Set<Biome> convertIsKnownArgument(StructurePrepareContext structurePrepareContext)
        {
            return BiomeMatcher.gatherAllBiomes();
        }

        @Override
        public BiomeMatcher createCache(String var)
        {
            return new BiomeMatcher(var);
        }
    }

    public static class DimensionVariableType extends DelegatingVariableType<Boolean, Argument, StructurePrepareContext, WorldProvider, Object, DimensionMatcher>
    {
        public DimensionVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public WorldProvider convertEvaluateArgument(Argument argument)
        {
            return argument.context.world.provider;
        }

        @Override
        public DimensionMatcher createCache(String var)
        {
            return new DimensionMatcher(var);
        }
    }

    public static class DependencyVariableType extends DelegatingVariableType<Boolean, Argument, StructurePrepareContext, Object, Object, DependencyMatcher>
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
