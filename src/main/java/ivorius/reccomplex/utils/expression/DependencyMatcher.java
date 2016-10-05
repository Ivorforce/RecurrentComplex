/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils.expression;

import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.utils.algebra.BoolFunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import joptsimple.internal.Strings;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;

import java.util.Arrays;
import java.util.function.BooleanSupplier;

/**
 * Created by lukas on 19.09.14.
 */
public class DependencyMatcher extends BoolFunctionExpressionCache<Object, Object> implements BooleanSupplier
{
    public static final String MOD_PREFIX = "mod:";
    public static final String STRUCTURE_PREFIX = "structure:";

    public DependencyMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "No Dependencies", expression);

        addTypes(new ModVariableType(MOD_PREFIX, ""), t -> t.alias("$", ""));
        addTypes(new RegistryVariableType(STRUCTURE_PREFIX, "", StructureRegistry.INSTANCE), t -> t.alias("#", ""), t -> t.alias("strc:", ""));

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
        return test(null);
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

    protected static class RegistryVariableType extends VariableType<Boolean, Object, Object>
    {
        public LeveledRegistry registry;

        public RegistryVariableType(String prefix, String suffix, LeveledRegistry registry)
        {
            super(prefix, suffix);
            this.registry = registry;
        }

        @Override
        public Boolean evaluate(String var, Object args)
        {
            return registry.has(var);
        }

        @Override
        public Validity validity(String var, Object args)
        {
            return evaluate(var, args) ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }
}
