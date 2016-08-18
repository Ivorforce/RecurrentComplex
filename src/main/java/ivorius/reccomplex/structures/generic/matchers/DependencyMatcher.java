/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import com.mojang.realmsclient.gui.ChatFormatting;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import net.minecraftforge.fml.common.Loader;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.utils.*;
import joptsimple.internal.Strings;

import java.util.Arrays;

/**
 * Created by lukas on 19.09.14.
 */
public class DependencyMatcher extends PrefixedTypeExpressionCache<Boolean>
{
    public static final String MOD_PREFIX = "$";
    public static final String STRUCTURE_PREFIX = "#";

    public DependencyMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, ChatFormatting.GREEN + "No Dependencies", expression);

        addType(new ModVariableType(MOD_PREFIX));
        addType(new StructureVariableType(STRUCTURE_PREFIX));
    }

    public static String ofMods(String... ids)
    {
        return ids.length > 0
                ? MOD_PREFIX + Strings.join(Arrays.asList(ids), " & " + MOD_PREFIX)
                : "";
    }

    public boolean apply()
    {
        return evaluate();
    }

    protected static class ModVariableType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public ModVariableType(String prefix)
        {
            super(prefix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            return Loader.isModLoaded(var);
        }

        @Override
        public boolean isKnown(String var, Object... args)
        {
            return evaluate(var, args);
        }
    }

    protected static class StructureVariableType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public StructureVariableType(String prefix)
        {
            super(prefix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            return StructureRegistry.INSTANCE.hasStructure(var);
        }

        @Override
        public boolean isKnown(String var, Object... args)
        {
            return evaluate(var, args);
        }
    }
}
