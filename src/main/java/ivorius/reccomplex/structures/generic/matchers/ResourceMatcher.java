/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.utils.ExpressionCaches;
import ivorius.reccomplex.utils.PrefixedTypeExpressionCache;
import ivorius.reccomplex.utils.RCBoolAlgebra;
import net.minecraft.util.EnumChatFormatting;

/**
 * Created by lukas on 01.05.15.
 */
public class ResourceMatcher extends PrefixedTypeExpressionCache<Boolean>
{
    public static final String DOMAIN_PREFIX = "$";

    public ResourceMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, EnumChatFormatting.GREEN + "Any Resource", expression);
        addType(new ResourceIDType(""));
        addType(new DomainType(DOMAIN_PREFIX));
    }

    public boolean apply(String resourceID, String domain)
    {
        return evaluate(resourceID, domain);
    }

    protected static class ResourceIDType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public ResourceIDType(String prefix)
        {
            super(prefix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            return args[0].equals(var);
        }

        @Override
        public boolean isKnown(final String var, final Object... args)
        {
            return StructureRegistry.INSTANCE.hasStructure(var);
        }
    }

    protected static class DomainType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public DomainType(String prefix)
        {
            super(prefix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            return args[1].equals(var);
        }

        @Override
        public boolean isKnown(final String var, final Object... args)
        {
            return true;
        }
    }
}
