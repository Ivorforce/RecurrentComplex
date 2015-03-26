/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.utils.ExpressionCache;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;

/**
 * Created by lukas on 26.03.15.
 */
public class TableDataSourceExpression<T> implements TableDataSource, TableElementPropertyListener
{
    public String title;
    private String tooltip;

    public ExpressionCache<T> expressionCache;

    protected TableElementTitle parsed;

    public TableDataSourceExpression(String title, String tooltip, ExpressionCache<T> expressionCache)
    {
        this.title = title;
        this.tooltip = tooltip;
        this.expressionCache = expressionCache;
    }

    public static String parsedString(ExpressionCache expressionCache)
    {
        if (expressionCache.isExpressionValid())
            return expressionCache.getDisplayString();
        else
        {
            ParseException parseException = expressionCache.getParseException();
            return String.format("%s%s%s: at %d", EnumChatFormatting.RED, parseException.getMessage(), EnumChatFormatting.RESET,
                    parseException.getErrorOffset());
        }
    }

    public static GuiValidityStateIndicator.State getValidityState(ExpressionCache<?> expressionCache)
    {
        return !expressionCache.isExpressionValid()
                ? GuiValidityStateIndicator.State.INVALID
                : expressionCache.containsUnknownVariables()
                ? GuiValidityStateIndicator.State.SEMI_VALID
                : GuiValidityStateIndicator.State.VALID;
    }

    @Override
    public int numberOfElements()
    {
        return 2;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index == 0)
        {
            TableElementString element = new TableElementString("expression", title, expressionCache.getExpression());
            if (tooltip != null)
                element.setTooltip(IvTranslations.formatLines(tooltip));
            element.setShowsValidityState(true);
            element.setValidityState(getValidityState(expressionCache));
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 1)
        {
            parsed = new TableElementTitle("parsedExpression", "", StringUtils.abbreviate(parsedString(expressionCache), 60));
            parsed.setPositioning(TableElementTitle.Positioning.TOP);
            return parsed;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("expression".equals(element.getID()))
        {
            expressionCache.setExpression((String) element.getPropertyValue());
            ((TableElementString) element).setValidityState(getValidityState(expressionCache));
            if (parsed != null)
                parsed.setDisplayString(StringUtils.abbreviate(parsedString(expressionCache), 60));
        }
    }
}
