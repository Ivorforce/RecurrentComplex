/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils.expression;

import ivorius.reccomplex.files.RCFileSaver;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.saving.FileSaver;
import ivorius.reccomplex.utils.algebra.BoolFunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import joptsimple.internal.Strings;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by lukas on 19.09.14.
 */
public class DependencyMatcher extends BoolFunctionExpressionCache<FileSaver, FileSaver>
{
    public static final String MOD_PREFIX = "mod:";
    public static final String REGISTRY_PREFIX = "registry:";

    public DependencyMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "No Dependencies", expression);

        addTypes(new ModVariableType(MOD_PREFIX, ""), t -> t.alias("$", ""));
        addTypes(new RegistryVariableType(REGISTRY_PREFIX, ""), t -> t.alias("reg:", ""));
        // legacy
        addTypes(new RegistryHasVariableType("structure:", "", RCFileSaver.STRUCTURE), t -> t.alias("#", ""), t -> t.alias("strc:", ""));

        testVariables();
    }

    public static String ofMods(String... ids)
    {
        return ids.length > 0
                ? MOD_PREFIX + Strings.join(Arrays.asList(ids), " & " + MOD_PREFIX)
                : "";
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

    protected static class RegistryHasVariableType extends VariableType<Boolean, FileSaver, FileSaver>
    {
        public String registryID;

        public RegistryHasVariableType(String prefix, String suffix, String registry)
        {
            super(prefix, suffix);
            this.registryID = registry;
        }

        @Override
        public Boolean evaluate(String var, FileSaver saver)
        {
            return saver.registry(registryID).has(var);
        }

        @Override
        public Validity validity(String var, FileSaver saver)
        {
            return !saver.has(registryID) ? Validity.ERROR
                    : saver.registry(registryID).has(var) ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }

    protected static class RegistryVariableType extends DelegatingVariableType<Boolean, FileSaver, FileSaver, LeveledRegistry, LeveledRegistry, RegistryMatcher>
    {
        public RegistryVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        public static Optional<Pair<String, String>> splitOnce(String var, String split)
        {
            int index = var.indexOf(split);
            return index >= 0 ? Optional.of(Pair.of(var.substring(0, index), var.substring(index + 1, var.length()))) : Optional.empty();
        }

        @Override
        public Validity validity(String var, FileSaver fileSaver)
        {
            return !splitOnce(var, ".").isPresent() ? Validity.ERROR : super.validity(var, fileSaver);
        }

        @Override
        protected String getVarRepresentation(String var, FileSaver fileSaver)
        {
            return splitOnce(var, ".").map(p -> TextFormatting.BLUE + p.getLeft() + TextFormatting.YELLOW + "."
                    + TextFormatting.RESET + super.getVarRepresentation(var, fileSaver))
                    .orElse(getRepresentation(Validity.ERROR) + var);
        }

        @Override
        public RegistryMatcher createCache()
        {
            return new RegistryMatcher("");
        }

        @Override
        public Boolean evaluate(String var, FileSaver fileSaver)
        {
            return splitOnce(var, ".").map(p -> cache.evaluateVariable(p.getRight(), convertEvaluateArgument(var, fileSaver))).orElseThrow(IllegalStateException::new);
        }

        @Override
        public LeveledRegistry convertArgument(String var, FileSaver fileSaver)
        {
            return splitOnce(var, ".").map(p -> fileSaver.registry(p.getLeft())).orElseThrow(IllegalStateException::new);
        }
    }
}
