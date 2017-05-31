/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.expression;

import ivorius.reccomplex.utils.algebra.BoolFunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import ivorius.reccomplex.utils.algebra.SupplierCache;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import net.minecraft.util.text.TextFormatting;

import java.text.ParseException;
import java.util.function.Function;

/**
 * Created by lukas on 01.05.15.
 */
public class GenerationTypeExpression extends BoolFunctionExpressionCache<GenerationType, Object>
{
    public static final String ID_PREFIX = "id=";
    public static final String TYPE_PREFIX = "type=";

    public GenerationTypeExpression()
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Generation");

        addTypes(new IdentifierType(ID_PREFIX, ""));
        addTypes(new TypeType(TYPE_PREFIX, ""), t -> t.alias("$", ""));
    }

    protected static class IdentifierType extends VariableType<Boolean, GenerationType, Object>
    {
        public IdentifierType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Function<SupplierCache<GenerationType>, Boolean> parse(String var) throws ParseException
        {
            return info -> info.get() != null && var.equals(info.get().id());
        }

        @Override
        public Validity validity(final String var, final Object args)
        {
            return Validity.KNOWN;
        }
    }

    protected static class TypeType extends VariableType<Boolean, GenerationType, Object>
    {
        public TypeType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Function<SupplierCache<GenerationType>, Boolean> parse(String var) throws ParseException
        {
            Class<? extends GenerationType> theClass = StructureRegistry.GENERATION_TYPES.typeForID(var);
            return theClass != null
                    ? info -> info.get() != null && theClass.isAssignableFrom(info.get().getClass())
                    : info -> false;
        }

        @Override
        public Validity validity(final String var, final Object args)
        {
            return StructureRegistry.GENERATION_TYPES.typeForID(var) != null ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }
}
