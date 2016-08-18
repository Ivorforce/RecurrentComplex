/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.mojang.realmsclient.gui.ChatFormatting;

/**
 * Created by lukas on 24.03.15.
 */
public class ExpressionCaches
{
    public static abstract class SimpleVariableType<T> extends PrefixedTypeExpressionCache.VariableType<T>
    {
        public SimpleVariableType(String prefix)
        {
            super(prefix);
        }

        @Override
        public String getRepresentation(String var, Object... args)
        {
            ChatFormatting variableColor = isKnown(var, args) ? ChatFormatting.GREEN : ChatFormatting.YELLOW;
            return ChatFormatting.BLUE + prefix + variableColor + var + ChatFormatting.RESET;
        }
    }
}
