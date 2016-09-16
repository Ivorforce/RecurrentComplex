/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import ivorius.reccomplex.structures.Environment;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
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
public class EnvironmentMatcher extends FunctionExpressionCache<Boolean, Environment, Object> implements Predicate<Environment>
{
    public static final String BIOME_PREFIX = "biome.";
    public static final String DIMENSION_PREFIX = "dimension.";
    public static final String DEPENDENCY_PREFIX = "dependency.";
    public static final String VILLAGE_TYPE_PREFIX = "villagetype=";
    public static final String GENERATION_INFO_PREFIX = "generation.";

    public EnvironmentMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Always", expression);

        addType(unknown(false));
        addType(new BiomeVariableType(BIOME_PREFIX, ""));
        addTypes(new DimensionVariableType(DIMENSION_PREFIX, ""), t -> t.alias("dim.", ""));
        addTypes(new DependencyVariableType(DEPENDENCY_PREFIX, ""), t -> t.alias("dep.", ""));
        addTypes(new VillageTypeType(VILLAGE_TYPE_PREFIX, ""), t -> t.alias("vtype.", ""));
        addTypes(new VillageTypeType(GENERATION_INFO_PREFIX, ""), t -> t.alias("gen.", ""));

        testVariables();
    }

    @Override
    public boolean test(Environment argument)
    {
        return evaluate(argument);
    }

    public static class BiomeVariableType extends DelegatingVariableType<Boolean, Environment, Object, Biome, Set<Biome>, BiomeMatcher>
    {
        public BiomeVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Biome convertEvaluateArgument(Environment argument)
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

    public static class DimensionVariableType extends DelegatingVariableType<Boolean, Environment, Object, WorldProvider, Object, DimensionMatcher>
    {
        public DimensionVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public WorldProvider convertEvaluateArgument(Environment argument)
        {
            return argument.world.provider;
        }

        @Override
        public DimensionMatcher createCache(String var)
        {
            return new DimensionMatcher(var);
        }
    }

    public static class DependencyVariableType extends DelegatingVariableType<Boolean, Environment, Object, Object, Object, DependencyMatcher>
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

    protected static class VillageTypeType extends DelegatingVariableType<Boolean, Environment, Object, StructureGenerationInfo, Object, StructureGenerationInfoMatcher>
    {
        public VillageTypeType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public StructureGenerationInfo convertEvaluateArgument(Environment environment)
        {
            return environment.generationInfo;
        }

        @Override
        protected StructureGenerationInfoMatcher createCache(String var)
        {
            return new StructureGenerationInfoMatcher(var);
        }
    }
}
