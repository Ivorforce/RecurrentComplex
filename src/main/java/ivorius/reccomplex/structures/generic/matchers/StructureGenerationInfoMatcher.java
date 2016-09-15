/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.utils.FunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.function.Predicate;

/**
 * Created by lukas on 01.05.15.
 */
public class StructureGenerationInfoMatcher extends FunctionExpressionCache<Boolean, StructureGenerationInfo, Object> implements Predicate<StructureGenerationInfo>
{
    public static final String ID_PREFIX = "id=";
    public static final String TYPE_PREFIX = "type=";

    public StructureGenerationInfoMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Generation", expression);
        addTypes(new IdentifierType(ID_PREFIX, ""));
        addTypes(new TypeType(TYPE_PREFIX, ""), t -> t.alias("$", ""));

        testVariables();
    }

    @Override
    public boolean test(StructureGenerationInfo info)
    {
        return evaluate(info);
    }

    protected static class IdentifierType extends VariableType<Boolean, StructureGenerationInfo, Object>
    {
        public IdentifierType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, StructureGenerationInfo info)
        {
            return info != null && var.equals(info.id());
        }

        @Override
        public Validity validity(final String var, final Object args)
        {
            return Validity.KNOWN;
        }
    }

    protected static class TypeType extends VariableType<Boolean, StructureGenerationInfo, Object>
    {
        public TypeType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, StructureGenerationInfo info)
        {
            return info != null && var.equals(StructureRegistry.INSTANCE.getGenerationInfoRegistry().iDForType(info.getClass()));
        }

        @Override
        public Validity validity(final String var, final Object args)
        {
            return StructureRegistry.INSTANCE.getGenerationInfoRegistry().typeForID(var) != null ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }
}
