/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.expression;

import com.google.common.primitives.Ints;
import ivorius.reccomplex.utils.algebra.BoolFunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import ivorius.reccomplex.utils.algebra.SupplierCache;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextFormatting;

import java.text.ParseException;
import java.util.function.Function;

/**
 * Created by lukas on 01.05.15.
 */
public class CommandExpression extends BoolFunctionExpressionCache<CommandExpression.Argument, Object>
{
    public static final String NAME_PREFIX = "name=";
    public static final String PERM_PREFIX = "canUseLevel:";

    public CommandExpression()
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Command");

        addTypes(new NameType(NAME_PREFIX, ""), t -> t.alias("$", ""));
        addTypes(new PermType(PERM_PREFIX, ""), t -> t.alias("#", ""));
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

    protected class NameType extends VariableType<Boolean, CommandExpression.Argument, Object>
    {
        public NameType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Function<SupplierCache<Argument>, Boolean> parse(String var)
        {
            return argument -> argument.get().sender.getName().equals(var);
        }

        @Override
        public Validity validity(final String var, final Object object)
        {
            return Validity.KNOWN;
        }
    }

    protected class PermType extends VariableType<Boolean, CommandExpression.Argument, Object>
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
        public Function<SupplierCache<Argument>, Boolean> parse(String var) throws ParseException
        {
            Integer level = parseNumber(var);
            if (level == null)
                throw new ParseException("Not a number: " + var, 0); // TODO WHERE??
            return argument -> argument.get().sender.canCommandSenderUseCommand(level, argument.get().name);
        }

        @Override
        public Validity validity(final String var, final Object object)
        {
            return Validity.KNOWN;
        }
    }
}
