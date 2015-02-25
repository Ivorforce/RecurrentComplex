/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import ivorius.reccomplex.json.RCGsonHelper;
import ivorius.reccomplex.utils.ExpressionCache;
import ivorius.reccomplex.utils.Visitor;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lukas on 19.09.14.
 */
public class BiomeMatcher extends ExpressionCache implements Predicate<BiomeGenBase>
{
    public static final String BIOME_TYPE_PREFIX = "$";

    public BiomeMatcher(String expression)
    {
        super(expression);
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
        return var.startsWith(BiomeMatcher.BIOME_TYPE_PREFIX)
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
                return (isKnownVariable(input, biomes) ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) + input + EnumChatFormatting.RESET;
            }
        }) : EnumChatFormatting.RED + expression;
    }
}
