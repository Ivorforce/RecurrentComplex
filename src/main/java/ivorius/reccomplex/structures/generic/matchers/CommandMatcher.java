/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import com.google.common.primitives.Ints;
import ivorius.reccomplex.utils.ExpressionCaches;
import ivorius.reccomplex.utils.FunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * Created by lukas on 01.05.15.
 */
public class CommandMatcher extends FunctionExpressionCache<Boolean>
{
    public static final String NAME_PREFIX = "name=";
    public static final String PERM_PREFIX = "canUseLevel(";
    public static final String PERM_SUFFIX = ")";

    public CommandMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Command", expression);
        addTypes(new NameType(NAME_PREFIX, ""), t -> t.alias("$", ""));
        addTypes(new PermType(PERM_PREFIX, PERM_SUFFIX), t -> t.alias("#", ""));
    }

    public boolean apply(String commandName, ICommandSender sender)
    {
        return evaluate(commandName, sender);
    }

    protected static class NameType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public NameType(String prefix, String suffix)
        {
            super(prefix, suffix);
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
        public PermType(String prefix, String suffix)
        {
            super(prefix, suffix);
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
