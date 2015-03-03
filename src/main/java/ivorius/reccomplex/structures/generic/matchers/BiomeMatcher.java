/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.json.RCGsonHelper;
import ivorius.reccomplex.utils.BoolAlgebra;
import ivorius.reccomplex.utils.ExpressionCache;
import ivorius.reccomplex.utils.Visitor;
import joptsimple.internal.Strings;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lukas on 19.09.14.
 */
public class BiomeMatcher extends ExpressionCache<Boolean> implements Predicate<BiomeGenBase>
{
    public static final String BIOME_TYPE_PREFIX = "$";

    public BiomeMatcher(String expression)
    {
        super(BoolAlgebra.algebra(), expression);
    }

    public static String ofTypes(BiomeDictionary.Type... biomeTypes)
    {
        return BIOME_TYPE_PREFIX + Strings.join(Lists.transform(Arrays.asList(biomeTypes), new Function<BiomeDictionary.Type, String>()
        {
            @Nullable
            @Override
            public String apply(@Nullable BiomeDictionary.Type input)
            {
                return input != null ? IvGsonHelper.serializedName(input) : null;
            }
        }), " & " + BIOME_TYPE_PREFIX);
    }

    public static Set<BiomeGenBase> gatherAllBiomes()
    {
        Set<BiomeGenBase> set = new HashSet<>();

        for (BiomeGenBase biomeGenBase : BiomeGenBase.getBiomeGenArray())
        {
            if (biomeGenBase != null)
                set.add(biomeGenBase);
        }

        for (BiomeDictionary.Type type : BiomeDictionary.Type.values())
            Collections.addAll(set, BiomeDictionary.getBiomesForType(type));

        return set;
    }

    public static boolean isKnownVariable(final String var, Set<BiomeGenBase> biomes)
    {
        return var.startsWith(BIOME_TYPE_PREFIX)
                ? RCGsonHelper.enumForNameIgnoreCase(var.substring(BIOME_TYPE_PREFIX.length()), BiomeDictionary.Type.values()) != null
                : Iterables.any(biomes, new Predicate<BiomeGenBase>()
        {
            @Override
            public boolean apply(BiomeGenBase input)
            {
                return input.biomeName.equals(var);
            }
        });
    }

    public boolean containsUnknownVariables()
    {
        if (parsedExpression != null)
        {
            final Set<BiomeGenBase> biomes = gatherAllBiomes();

            return !parsedExpression.walkVariables(new Visitor<String>()
            {
                @Override
                public boolean visit(final String s)
                {
                    return isKnownVariable(s, biomes);
                }
            });
        }

        return true;
    }

    @Override
    public boolean apply(final BiomeGenBase input)
    {
        return parsedExpression != null && parsedExpression.evaluate(new Function<String, Boolean>()
        {
            @Override
            public Boolean apply(String var)
            {
                if (var.startsWith(BIOME_TYPE_PREFIX))
                    return BiomeDictionary.isBiomeOfType(input,
                            RCGsonHelper.enumForNameIgnoreCase(var.substring(BIOME_TYPE_PREFIX.length()),
                                    BiomeDictionary.Type.values()));

                return input.biomeName.equals(var);
            }
        });
    }

    @Nonnull
    @Override
    public String getDisplayString()
    {
        final Set<BiomeGenBase> biomes = gatherAllBiomes();

        return parsedExpression != null ? parsedExpression.toString(new Function<String, String>()
        {
            @Nullable
            @Override
            public String apply(String input)
            {
                EnumChatFormatting variableColor = isKnownVariable(input, biomes) ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW;

                if (input.startsWith(BIOME_TYPE_PREFIX))
                    return EnumChatFormatting.BLUE + BIOME_TYPE_PREFIX + variableColor + input.substring(BIOME_TYPE_PREFIX.length()) + EnumChatFormatting.RESET;
                return variableColor + input + EnumChatFormatting.RESET;
            }
        }) : EnumChatFormatting.RED + expression;
    }
}
