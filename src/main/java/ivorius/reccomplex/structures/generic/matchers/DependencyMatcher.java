/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.Loader;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.json.RCGsonHelper;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.utils.ExpressionCache;
import ivorius.reccomplex.utils.RCBoolAlgebra;
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
public class DependencyMatcher extends ExpressionCache<Boolean>
{
    public static final String MOD_PREFIX = "$";
    public static final String STRUCTURE_PREFIX = "#";

    public DependencyMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, EnumChatFormatting.GREEN + "No Dependencies", expression);
    }

    public static String ofMods(String... ids)
    {
        return ids.length > 0
                ? MOD_PREFIX + Strings.join(Arrays.asList(ids), " & " + MOD_PREFIX)
                : "";
    }

    public static boolean isKnownVariable(final String var)
    {
        return var.startsWith(MOD_PREFIX)
                ? Loader.isModLoaded(var.substring(MOD_PREFIX.length()))
                : var.startsWith(STRUCTURE_PREFIX)
                && StructureRegistry.hasStructure(var.substring(STRUCTURE_PREFIX.length()));
    }

    public boolean containsUnknownVariables()
    {
        if (parsedExpression != null)
        {
            return !parsedExpression.walkVariables(new Visitor<String>()
            {
                @Override
                public boolean visit(final String s)
                {
                    return isKnownVariable(s);
                }
            });
        }

        return true;
    }

    public boolean apply()
    {
        return parsedExpression != null && parsedExpression.evaluate(new Function<String, Boolean>()
        {
            @Override
            public Boolean apply(String var)
            {
                return isKnownVariable(var);
            }
        });
    }

    @Nonnull
    @Override
    public String getDisplayString()
    {
        return parsedExpression != null ? parsedExpression.toString(new Function<String, String>()
        {
            @Nullable
            @Override
            public String apply(String input)
            {
                EnumChatFormatting variableColor = isKnownVariable(input) ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW;

                if (input.startsWith(MOD_PREFIX))
                    return EnumChatFormatting.BLUE + MOD_PREFIX + variableColor + input.substring(MOD_PREFIX.length()) + EnumChatFormatting.RESET;
                if (input.startsWith(STRUCTURE_PREFIX))
                    return EnumChatFormatting.AQUA + STRUCTURE_PREFIX + variableColor + input.substring(STRUCTURE_PREFIX.length()) + EnumChatFormatting.RESET;
                return variableColor + input + EnumChatFormatting.RESET;
            }
        }) : EnumChatFormatting.RED + expression;
    }
}
