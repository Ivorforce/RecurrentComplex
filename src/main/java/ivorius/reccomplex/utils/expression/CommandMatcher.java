/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.expression;

import com.google.common.primitives.Ints;
import ivorius.reccomplex.utils.algebra.BoolFunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextFormatting;

/**
 * Created by lukas on 01.05.15.
 */
public class CommandMatcher extends BoolFunctionExpressionCache<CommandMatcher.Argument, Object>
{
    public static final String NAME_PREFIX = "name=";
    public static final String PERM_PREFIX = "canUseLevel:";

    public CommandMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Command", expression);

        addTypes(new NameType(NAME_PREFIX, ""), t -> t.alias("$", ""));
        addTypes(new PermType(PERM_PREFIX, ""), t -> t.alias("#", ""));

        testVariables();
    }

    public static class Argument
    {
        public final String name;
        public final ICommandSender sender;

        public Argument(String name, ICommandSender sender)
        {
            this.name = name;
            this.sender = sender;
        }
    }

    protected class NameType extends VariableType<Boolean, CommandMatcher.Argument, Object>
    {
        public NameType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, Argument argument)
        {
            return argument.sender.getName().equals(var);
        }

        @Override
        public Validity validity(final String var, final Object object)
        {
            return Validity.KNOWN;
        }
    }

    protected class PermType extends VariableType<Boolean, CommandMatcher.Argument, Object>
    {
        public PermType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        public Integer parseNumber(String var)
        {
            Integer integer = Ints.tryParse(var);
            return integer != null ? integer : 0;
        }

        @Override
        public Boolean evaluate(String var, Argument argument)
        {
            return argument.sender.canUseCommand(parseNumber(var), argument.name);
        }

        @Override
        public Validity validity(final String var, final Object object)
        {
            return Validity.KNOWN;
        }
    }
}
