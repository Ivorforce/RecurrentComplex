/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TableCellTitle;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.utils.RCStrings;
import ivorius.reccomplex.utils.algebra.FunctionExpressionCaches;
import ivorius.reccomplex.utils.expression.*;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.utils.algebra.FunctionExpressionCache;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Created by lukas on 26.03.15.
 */
public class TableDataSourceExpression<T, U, E extends FunctionExpressionCache<T, ?, U>> implements TableDataSource
{
    public String title;
    private List<String> tooltip;
    private List<String> expressionTooltip;

    public E e;
    public U u;

    protected TableCellTitle parsed;
    protected TableCellString expressionCell;

    @Nullable
    protected BooleanSupplier enabledSupplier;

    public TableDataSourceExpression(String title, List<String> tooltip, List<String> expressionTooltip, E e, U u)
    {
        this.title = title;
        this.tooltip = tooltip;
        this.expressionTooltip = expressionTooltip;
        this.e = e;
        this.u = u;
    }

    public static <T, U, E extends FunctionExpressionCache<T, ?, U>> TableDataSourceExpression<T, U, E> constructDefault(String title, E e, U u)
    {
        return constructDefault(title, null, e, u);
    }

    public static <T, U, E extends FunctionExpressionCache<T, ?, U>> TableDataSourceExpression<T, U, E> constructDefault(String title, List<String> tooltip, E e, U u)
    {
        if (e instanceof BiomeExpression)
            return new TableDataSourceExpression<>(title, tooltip, IvTranslations.formatLines("reccomplex.expression.biome.tooltip"), e, u);
        else if (e instanceof BlockExpression)
            return new TableDataSourceExpression<>(title, tooltip, IvTranslations.formatLines("reccomplex.expression.block.tooltip"), e, u);
        else if (e instanceof PositionedBlockExpression)
            return new TableDataSourceExpression<>(title, tooltip, IvTranslations.formatLines("reccomplex.expression.positioned_block.tooltip"), e, u);
        else if (e instanceof DependencyExpression)
            return new TableDataSourceExpression<>(title, tooltip, IvTranslations.formatLines("reccomplex.expression.dependency.tooltip"), e, u);
        else if (e instanceof DimensionExpression)
            return new TableDataSourceExpression<>(title, tooltip, IvTranslations.formatLines("reccomplex.expression.dimension.tooltip"), e, u);
        else if (e instanceof EnvironmentExpression)
            return new TableDataSourceExpression<>(title, tooltip, IvTranslations.formatLines("reccomplex.expression.environment.tooltip"), e, u);

        throw new IllegalArgumentException();
    }

    public static <U> String parsedString(FunctionExpressionCache<?, ?, U> expressionCache, U u)
    {
        if (expressionCache.isExpressionValid())
            return expressionCache.getDisplayString(u);
        else
            return FunctionExpressionCaches.readableException(expressionCache);
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

    public TableDataSourceExpression<T, U, E> enabled(BooleanSupplier enabledSupplier)
    {
        this.enabledSupplier = enabledSupplier;
        return this;
    }

    @Override
    public int numberOfCells()
    {
        return 2;
    }

    @Override
    public TableCell cellForIndex(GuiTable table, int index)
    {
        if (index == 0)
        {
            expressionCell = new TableCellString("expression", e.getExpression());
            expressionCell.setTooltip(expressionTooltip);
            expressionCell.setEnabled(canEdit());
            expressionCell.setShowsValidityState(true);
            expressionCell.setValidityState(getValidityState(e, u));
            expressionCell.addListener(val -> {
                e.setExpression(val);
                expressionCell.setValidityState(getValidityState(e, u));
            });
            expressionCell.setChangeListener(() -> {
                if (parsed != null)
                    parsed.setDisplayString(parsedString());
            });
            return new TitledCell(title, expressionCell).withTitleTooltip(tooltip);
        }
        else if (index == 1)
        {
            parsed = new TableCellTitle("parsedExpression", parsedString());
            parsed.setPositioning(TableCellTitle.Positioning.TOP);
            return new TitledCell(parsed);
        }

        return null;
    }

    @Nullable
    protected String parsedString()
    {
        return RCStrings.abbreviateFormatted(parsedString(e, u), getCursorOffset(), 71);
    }

    protected int getCursorOffset()
    {
        int offset = 0;
        if (expressionCell != null && expressionCell.getTextField() != null)
            offset = expressionCell.getTextField().getCursorPosition();
        return offset;
    }

    public boolean canEdit()
    {
        return enabledSupplier == null || enabledSupplier.getAsBoolean();
    }
}
