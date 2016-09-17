/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import net.minecraft.util.text.TextFormatting;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import net.minecraftforge.fml.common.Loader;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.utils.*;
import joptsimple.internal.Strings;

import java.util.Arrays;
import java.util.function.BooleanSupplier;

/**
 * Created by lukas on 19.09.14.
 */
public class DependencyMatcher extends FunctionExpressionCache<Boolean, Object, Object> implements BooleanSupplier
{
    public static final String MOD_PREFIX = "mod:";
    public static final String STRUCTURE_PREFIX = "structure:";

    public DependencyMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "No Dependencies", expression);

        addTypes(new ModVariableType(MOD_PREFIX, ""), t -> t.alias("$", ""));
        addTypes(new StructureVariableType(STRUCTURE_PREFIX, ""), t -> t.alias("#", ""), t -> t.alias("strc:", ""));

        testVariables();
    }

    public static String ofMods(String... ids)
    {
        return ids.length > 0
                ? MOD_PREFIX + Strings.join(Arrays.asList(ids), " & " + MOD_PREFIX)
                : "";
    }

    @Override
    public boolean getAsBoolean()
    {
        return evaluate(null);
    }

    protected static class ModVariableType extends VariableType<Boolean, Object, Object>
    {
        public ModVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, Object args)
        {
            return Loader.isModLoaded(var);
        }

        @Override
        public Validity validity(String var, Object args)
        {
            return evaluate(var, args) ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }

    protected static class StructureVariableType extends VariableType<Boolean, Object, Object>
    {
        public StructureVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, Object args)
        {
            return StructureRegistry.INSTANCE.hasStructure(var);
        }

        @Override
        public Validity validity(String var, Object args)
        {
            return evaluate(var, args) ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }
}
