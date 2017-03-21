/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils.expression;

import com.google.common.collect.Lists;
import ivorius.reccomplex.utils.BiomeDictionaryAccessor;
import ivorius.reccomplex.utils.algebra.BoolFunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import ivorius.reccomplex.utils.algebra.SupplierCache;
import joptsimple.internal.Strings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lukas on 19.09.14.
 */
public class BiomeMatcher extends BoolFunctionExpressionCache<Biome, Object>
{
    public static final String BIOME_NAME_PREFIX = "name=";
    public static final String BIOME_ID_PREFIX = "id=";
    public static final String BIOME_TYPE_PREFIX = "type=";

    public BiomeMatcher()
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Biome");

        addTypes(new BiomeNameVariableType(BIOME_NAME_PREFIX, ""));
        addTypes(new BiomeIDVariableType(BIOME_ID_PREFIX, ""), t -> t.alias("", ""));
        addTypes(new BiomeDictVariableType(BIOME_TYPE_PREFIX, ""), t -> t.alias("$", ""));
    }

    public static String ofTypes(BiomeDictionary.Type... biomeTypes)
    {
        return BIOME_TYPE_PREFIX + Strings.join(Lists.transform(Arrays.asList(biomeTypes), input -> input != null ? input.getName() : null), " & " + BIOME_TYPE_PREFIX);
    }

    protected class BiomeNameVariableType extends VariableType<Boolean, Biome, Object>
    {
        public BiomeNameVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Function<SupplierCache<Biome>, Boolean> parse(String var)
        {
            List<Biome> biomes = Biome.REGISTRY.getKeys().stream()
                    .map(Biome.REGISTRY::getObject)
                    .filter(b -> b.getBiomeName().equals(var))
                    .collect(Collectors.toList());
            return o -> biomes.contains(o.get());
        }

        @Override
        public Validity validity(final String var, final Object biomes)
        {
            return Biome.REGISTRY.getKeys().stream().map(Biome.REGISTRY::getObject).anyMatch(b -> b.getBiomeName().equals(var))
                    ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }

    protected class BiomeIDVariableType extends VariableType<Boolean, Biome, Object>
    {
        public BiomeIDVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Function<SupplierCache<Biome>, Boolean> parse(String var)
        {
            Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(var));
            return b -> b.get() == biome;
        }

        @Override
        public Validity validity(final String var, final Object biomes)
        {
            return Biome.REGISTRY.containsKey(new ResourceLocation(var))
                    ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }

    protected class BiomeDictVariableType extends VariableType<Boolean, Biome, Object>
    {
        public BiomeDictVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Function<SupplierCache<Biome>, Boolean> parse(String var)
        {
            BiomeDictionary.Type type = BiomeDictionaryAccessor.getTypeWeak(var);
            return type != null ? b -> BiomeDictionary.hasType(b.get(), type)
                    || (type == BiomeDictionary.Type.WATER // Special test
                    && (BiomeDictionary.hasType(b.get(), BiomeDictionary.Type.OCEAN)
                    || BiomeDictionary.hasType(b.get(), BiomeDictionary.Type.RIVER))
            ) : b -> false;
        }

        @Override
        public Validity validity(String var, Object biomes)
        {
            return BiomeDictionaryAccessor.getTypeWeak(var) != null
                    ? Validity.KNOWN : Validity.UNKNOWN;
        }

    }
}
