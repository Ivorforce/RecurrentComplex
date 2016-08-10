/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import com.google.common.primitives.Ints;
import ivorius.reccomplex.utils.ExpressionCaches;
import ivorius.reccomplex.utils.PrefixedTypeExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

/**
 * Created by lukas on 01.05.15.
 */
public class CommandMatcher extends PrefixedTypeExpressionCache<Boolean>
{
    public static final String NAME_PREFIX = "$";
    public static final String PERM_PREFIX = "#";

    public CommandMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, EnumChatFormatting.GREEN + "Any Resource", expression);
        addType(new NameType(NAME_PREFIX));
        addType(new PermType(PERM_PREFIX));
    }

    public boolean apply(String commandName, ICommandSender sender)
    {
        return evaluate(commandName, sender);
    }

    protected static class NameType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public NameType(String prefix)
        {
            super(prefix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            return ((ICommandSender) args[1]).getName().equals(var);
        }

        @Override
        public boolean isKnown(final String var, final Object... args)
        {
            return true;
        }
    }

    protected static class PermType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public PermType(String prefix)
        {
            super(prefix);
        }

        public static Integer parseNumber(String var)
        {
            Integer integer = Ints.tryParse(var);
            return integer != null ? integer : 0;
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            return ((ICommandSender) args[1]).canCommandSenderUseCommand(parseNumber(var), (String) args[0]);
        }

        @Override
        public boolean isKnown(final String var, final Object... args)
        {
            return true;
        }
    }
}
