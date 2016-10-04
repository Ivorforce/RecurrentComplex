/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.matchers;

import ivorius.reccomplex.utils.FunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.function.Predicate;

/**
 * Created by lukas on 01.05.15.
 */
public class ResourceMatcher extends FunctionExpressionCache<Boolean, ResourceLocation, Object> implements Predicate<ResourceLocation>
{
    public static final String ID_PREFIX = "id=";
    public static final String DOMAIN_PREFIX = "domain=";

    public ResourceMatcher(String expression, Predicate<String> isKnown)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Structure", expression);

        addTypes(new ResourceIDType(ID_PREFIX, "", isKnown), t -> t.alias("", ""));
        addTypes(new DomainType(DOMAIN_PREFIX, ""), t -> t.alias("$", ""));

        testVariables();
    }

    @Override
    public boolean test(ResourceLocation location)
    {
        return evaluate(location);
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
        public Boolean evaluate(String var, ResourceLocation location)
        {
            return location.getResourcePath().equals(var);
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
        public Boolean evaluate(String var, ResourceLocation location)
        {
            return location.getResourceDomain().equals(var);
        }

        @Override
        public Validity validity(final String var, final Object args)
        {
            return Validity.KNOWN;
        }
    }
}
