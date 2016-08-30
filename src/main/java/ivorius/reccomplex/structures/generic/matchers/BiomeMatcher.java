/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.json.RCGsonHelper;
import ivorius.reccomplex.utils.ExpressionCaches;
import ivorius.reccomplex.utils.PrefixedTypeExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import joptsimple.internal.Strings;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 * Created by lukas on 19.09.14.
 */
public class BiomeMatcher extends PrefixedTypeExpressionCache<Boolean> implements Predicate<Biome>
{
    public static final String BIOME_TYPE_PREFIX = "$";

    public BiomeMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Biome", expression);

        addType(new BiomeVariableType(""));
        addType(new BiomeDictVariableType(BIOME_TYPE_PREFIX));
    }

    public static String ofTypes(BiomeDictionary.Type... biomeTypes)
    {
        return BIOME_TYPE_PREFIX + Strings.join(Lists.transform(Arrays.asList(biomeTypes), input -> input != null ? IvGsonHelper.serializedName(input) : null), " & " + BIOME_TYPE_PREFIX);
    }

    public static Set<Biome> gatherAllBiomes()
    {
        Set<Biome> set = new HashSet<>();

        for (Biome biome : Biome.REGISTRY)
        {
            if (biome != null)
                set.add(biome);
        }

        for (BiomeDictionary.Type type : BiomeDictionary.Type.values())
        {
            try
            {
                Collections.addAll(set, BiomeDictionary.getBiomesForType(type));
            }
            catch (Exception ignored) // list f'd up by a biome mod
            {

            }
        }

        return set;
    }

    @Override
    public boolean containsUnknownVariables()
    {
        return super.containsUnknownVariables(gatherAllBiomes());
    }

    @Nonnull
    @Override
    public String getDisplayString()
    {
        return super.getDisplayString(gatherAllBiomes());
    }

    @Override
    public boolean apply(final Biome input)
    {
        return evaluate(input);
    }

    protected static class BiomeVariableType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public BiomeVariableType(String prefix)
        {
            super(prefix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            return ((Biome) args[0]).getBiomeName().equals(var);
        }

        @Override
        public boolean isKnown(final String var, final Object... args)
        {
            return StreamSupport.stream(((Iterable<Biome>) args[0]).spliterator(), false).anyMatch(input -> input.getBiomeName().equals(var));
        }
    }

    protected static class BiomeDictVariableType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public BiomeDictVariableType(String prefix)
        {
            super(prefix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            BiomeDictionary.Type type = RCGsonHelper.enumForNameIgnoreCase(var, BiomeDictionary.Type.values());
            return type != null && BiomeDictionary.isBiomeOfType((Biome) args[0], type);
        }

        @Override
        public boolean isKnown(String var, Object... args)
        {
            return RCGsonHelper.enumForNameIgnoreCase(var, BiomeDictionary.Type.values()) != null;
        }
    }
}
