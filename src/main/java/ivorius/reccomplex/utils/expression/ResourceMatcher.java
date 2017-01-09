/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils.expression;

import ivorius.reccomplex.utils.algebra.BoolFunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import ivorius.reccomplex.utils.algebra.SupplierCache;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by lukas on 01.05.15.
 */
public class ResourceMatcher extends BoolFunctionExpressionCache<ResourceLocation, Object>
{
    public static final String ID_PREFIX = "id=";
    public static final String DOMAIN_PREFIX = "domain=";

    public ResourceMatcher(Predicate<String> isKnown)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Structure");

        addTypes(new ResourceIDType(ID_PREFIX, "", isKnown), t -> t.alias("", ""));
        addTypes(new DomainType(DOMAIN_PREFIX, ""), t -> t.alias("$", ""));
    }

    protected static class ResourceIDType extends VariableType<Boolean, ResourceLocation, Object>
    {
        private Predicate<String> isKnown;

        public ResourceIDType(String prefix, String suffix, Predicate<String> isKnown)
        {
            super(prefix, suffix);
            this.isKnown = isKnown;
        }

        @Override
        public Function<SupplierCache<ResourceLocation>, Boolean> parse(String var)
        {
            return location -> location.get().getResourcePath().equals(var);
        }

        @Override
        public Validity validity(final String var, final Object args)
        {
            return isKnown.test(var) ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }

    protected static class DomainType extends VariableType<Boolean, ResourceLocation, Object>
    {
        public DomainType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Function<SupplierCache<ResourceLocation>, Boolean> parse(String var)
        {
            return location -> location.get().getResourceDomain().equals(var);
        }

        @Override
        public Validity validity(final String var, final Object args)
        {
            return Validity.KNOWN;
        }
    }
}
