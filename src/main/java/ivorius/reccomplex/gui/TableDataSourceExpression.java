/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.matchers.BiomeMatcher;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import ivorius.reccomplex.structures.generic.matchers.DependencyMatcher;
import ivorius.reccomplex.structures.generic.matchers.DimensionMatcher;
import ivorius.reccomplex.utils.ExpressionCache;
import ivorius.ivtoolkit.tools.IvTranslations;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.List;

/**
 * Created by lukas on 26.03.15.
 */
public class TableDataSourceExpression<T> implements TableDataSource, TableCellPropertyListener
{
    public String title;
    private List<String> tooltip;

    public ExpressionCache<T> expressionCache;

    protected TableCellTitle parsed;

    public TableDataSourceExpression(String title, List<String> tooltip, ExpressionCache<T> expressionCache)
    {
        this.title = title;
        this.tooltip = tooltip;
        this.expressionCache = expressionCache;
    }
    
    public static <T> TableDataSourceExpression<T> constructDefault(String title, ExpressionCache<T> cache)
    {
        if (cache instanceof BiomeMatcher)
            return new TableDataSourceExpression<>(title, IvTranslations.formatLines("reccomplex.expression.biome.tooltip"), cache);
        else if (cache instanceof BlockMatcher)
            return new TableDataSourceExpression<>(title, IvTranslations.formatLines("reccomplex.expression.block.tooltip"), cache);
        else if (cache instanceof DependencyMatcher)
            return new TableDataSourceExpression<>(title, IvTranslations.formatLines("reccomplex.expression.dependency.tooltip"), cache);
        else if (cache instanceof DimensionMatcher)
            return new TableDataSourceExpression<>(title, IvTranslations.formatLines("reccomplex.expression.dimension.tooltip"), cache);

        throw new IllegalArgumentException();
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
            TableCellString cell = new TableCellString("expression", expressionCache.getExpression());
            if (tooltip != null)
                cell.setTooltip(tooltip);
            cell.setShowsValidityState(true);
            cell.setValidityState(getValidityState(expressionCache));
            cell.addPropertyListener(this);
            return new TableElementCell(title, cell);
        }
        else if (index == 1)
        {
            parsed = new TableCellTitle("parsedExpression", StringUtils.abbreviate(parsedString(expressionCache), 60));
            parsed.setPositioning(TableCellTitle.Positioning.TOP);
            return new TableElementCell(parsed);
        }

        return null;
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("expression".equals(cell.getID()))
        {
            expressionCache.setExpression((String) cell.getPropertyValue());
            ((TableCellString) cell).setValidityState(getValidityState(expressionCache));
            if (parsed != null)
                parsed.setDisplayString(StringUtils.abbreviate(parsedString(expressionCache), 60));
        }
    }
}
