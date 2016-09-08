/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.matchers.*;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.utils.FunctionExpressionCache;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.List;

/**
 * Created by lukas on 26.03.15.
 */
public class TableDataSourceExpression<T, U, E extends FunctionExpressionCache<T, ?, U>> implements TableDataSource
{
    public String title;
    private List<String> tooltip;

    public E e;
    public U u;

    protected TableCellTitle parsed;

    public TableDataSourceExpression(String title, List<String> tooltip, E e, U u)
    {
        this.title = title;
        this.tooltip = tooltip;
        this.e = e;
        this.u = u;
    }
    
    public static <T, U, E extends FunctionExpressionCache<T, ?, U>> TableDataSourceExpression<T, U, E> constructDefault(String title, E e, U u)
    {
        if (e instanceof BiomeMatcher)
            return new TableDataSourceExpression<>(title, IvTranslations.formatLines("reccomplex.expression.biome.tooltip"), e, u);
        else if (e instanceof BlockMatcher)
            return new TableDataSourceExpression<>(title, IvTranslations.formatLines("reccomplex.expression.block.tooltip"), e, u);
        else if (e instanceof DependencyMatcher)
            return new TableDataSourceExpression<>(title, IvTranslations.formatLines("reccomplex.expression.dependency.tooltip"), e, u);
        else if (e instanceof DimensionMatcher)
            return new TableDataSourceExpression<>(title, IvTranslations.formatLines("reccomplex.expression.dimension.tooltip"), e, u);
        else if (e instanceof GenerationMatcher)
            return new TableDataSourceExpression<>(title, IvTranslations.formatLines("reccomplex.expression.generation.tooltip"), e, u);

        throw new IllegalArgumentException();
    }

    public static <U> String parsedString(FunctionExpressionCache<?, ?, U> expressionCache, U u)
    {
        if (expressionCache.isExpressionValid())
            return expressionCache.getDisplayString(u);
        else
        {
            ParseException parseException = expressionCache.getParseException();
            return String.format("%s%s%s: at %d", TextFormatting.RED, parseException.getMessage(), TextFormatting.RESET,
                    parseException.getErrorOffset());
        }
    }

    public static <U> GuiValidityStateIndicator.State getValidityState(FunctionExpressionCache<?, ?, U> expressionCache, U u)
    {
        switch (expressionCache.validity(u))
        {
            case KNOWN:
                return GuiValidityStateIndicator.State.VALID;
            case UNKNOWN:
                return GuiValidityStateIndicator.State.SEMI_VALID;
            default:
                return GuiValidityStateIndicator.State.INVALID;
        }
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
            TableCellString cell = new TableCellString("expression", e.getExpression());
            if (tooltip != null)
                cell.setTooltip(tooltip);
            cell.setShowsValidityState(true);
            cell.setValidityState(getValidityState(e, u));
            cell.addPropertyConsumer(val -> {
                e.setExpression(val);
                cell.setValidityState(getValidityState(e, u));
                if (parsed != null)
                    parsed.setDisplayString(StringUtils.abbreviate(parsedString(e, u), 60));
            });
            return new TableElementCell(title, cell);
        }
        else if (index == 1)
        {
            parsed = new TableCellTitle("parsedExpression", StringUtils.abbreviate(parsedString(e, u), 60));
            parsed.setPositioning(TableCellTitle.Positioning.TOP);
            return new TableElementCell(parsed);
        }

        return null;
    }
}
