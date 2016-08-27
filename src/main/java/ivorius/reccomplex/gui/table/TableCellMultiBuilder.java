/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by lukas on 27.08.16.
 */
public class TableCellMultiBuilder
{
    protected final List<Supplier<String>> titles = new ArrayList<>();
    protected final List<Supplier<List<String>>> tooltips = new ArrayList<>();
    protected final List<Runnable> actions = new ArrayList<>();

    public TableNavigator navigator;
    public TableDelegate delegate;

    private TableCellMultiBuilder(TableNavigator navigator, TableDelegate delegate)
    {
        this.navigator = navigator;
        this.delegate = delegate;
    }

    public static TableCellMultiBuilder create(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableCellMultiBuilder(navigator, delegate);
    }

    public TableCellMultiBuilder addAction(Supplier<String> title, @Nullable Supplier<List<String>> tooltip, Runnable action)
    {
        titles.add(title);
        tooltips.add(tooltip);
        actions.add(() ->
        {
            action.run();
            delegate.reloadData();
        });
        return this;
    }

    public TableCellMultiBuilder addNavigation(Supplier<String> title, @Nullable Supplier<List<String>> tooltip, Supplier<GuiTable> table)
    {
        titles.add(title);
        tooltips.add(tooltip);
        actions.add(() ->
        {
            navigator.pushTable(table.get());
            delegate.reloadData();
        });
        return this;
    }

    @Nonnull
    public TableDataSource buildPreloaded(String title)
    {
        return new TableDataSourcePreloaded(buildElement(title));
    }

    @Nonnull
    public TableElementCell buildElement(String title)
    {
        return new TableElementCell(title, build());
    }

    @Nonnull
    public TableCell build()
    {
        List<TableCell> cells = new ArrayList<>();

        for (int i = 0; i < this.titles.size(); i++)
        {
            TableCellButton cell = new TableCellButton("action." + i, "action." + i, this.titles.get(i).get());
            int finalI = i;
            cell.addListener((cell1, action) -> actions.get(finalI).run());

            Supplier<List<String>> tooltip = this.tooltips.get(i);
            if (tooltip != null)
                cell.setTooltip(tooltip.get());

            cells.add(cell);
        }

        return new TableCellMulti(cells);
    }
}
