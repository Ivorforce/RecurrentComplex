/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.expression;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.saving.FileSaver;
import ivorius.reccomplex.utils.algebra.BoolFunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.StructureGenerationInfo;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;

/**
 * Created by lukas on 07.09.16.
 */
public class EnvironmentMatcher extends BoolFunctionExpressionCache<Environment, Object>
{
    public static final String BIOME_PREFIX = "biome.";
    public static final String DIMENSION_PREFIX = "dimension.";
    public static final String DEPENDENCY_PREFIX = "dependency.";
    public static final String VILLAGE_TYPE_PREFIX = "villagetype=";
    public static final String GENERATION_INFO_PREFIX = "generation.";

    public EnvironmentMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Always", expression);

        addType(new BiomeVariableType(BIOME_PREFIX, ""));
        addTypes(new DimensionVariableType(DIMENSION_PREFIX, ""), t -> t.alias("dim.", ""));
        addTypes(new DependencyVariableType(DEPENDENCY_PREFIX, ""), t -> t.alias("dep.", ""));
        addTypes(new VillageTypeType(VILLAGE_TYPE_PREFIX, ""), t -> t.alias("vtype.", ""));
        addTypes(new VillageTypeType(GENERATION_INFO_PREFIX, ""), t -> t.alias("gen.", ""));

        testVariables();
    }

    public static class BiomeVariableType extends DelegatingVariableType<Boolean, Environment, Object, Biome, Object, BiomeMatcher>
    {
        public BiomeVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Biome convertEvaluateArgument(String var, Environment argument)
        {
            return argument.biome;
        }

        @Override
        public BiomeMatcher createCache(String var, Environment a)
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
        public WorldProvider convertEvaluateArgument(String var, Environment argument)
        {
            return argument.world.provider;
        }

        @Override
        public DimensionMatcher createCache(String var, Environment a)
        {
            return new DimensionMatcher(var);
        }
    }

    public static class DependencyVariableType extends DelegatingVariableType<Boolean, Environment, Object, FileSaver, FileSaver, DependencyMatcher>
    {
        public DependencyVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public FileSaver convertArgument(String var, Environment environment)
        {
            return RecurrentComplex.saver;
        }

        @Override
        public DependencyMatcher createCache(String var, Environment a)
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
        public StructureGenerationInfo convertEvaluateArgument(String var, Environment environment)
        {
            return environment.generationInfo;
        }

        @Override
        protected StructureGenerationInfoMatcher createCache(String var, Environment a)
        {
            return new StructureGenerationInfoMatcher(var);
        }
    }
}
