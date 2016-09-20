/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table;

import ivorius.ivtoolkit.tools.IvTranslations;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Created by lukas on 27.08.16.
 */
public class TableCellMultiBuilder
{
    protected final List<Supplier<String>> titles = new ArrayList<>();
    protected final List<Supplier<List<String>>> tooltips = new ArrayList<>();
    protected final List<Runnable> actions = new ArrayList<>();
    protected final List<BooleanSupplier> enabledSuppliers = new ArrayList<>();

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
        enabledSuppliers.add(null);
        return this;
    }

    public TableCellMultiBuilder addNavigation(Supplier<String> title, @Nullable Supplier<List<String>> tooltip, Supplier<TableDataSource> dataSource)
    {
        titles.add(title);
        tooltips.add(tooltip);
        actions.add(() -> navigator.pushTable(new GuiTable(delegate, dataSource.get())));
        enabledSuppliers.add(null);
        return this;
    }

    public TableCellMultiBuilder addNavigation(Supplier<TableDataSource> dataSource)
    {
        return addNavigation(() -> IvTranslations.get("reccomplex.gui.edit"), null, dataSource);
    }

    public TableCellMultiBuilder enabled(BooleanSupplier enabled)
    {
        enabledSuppliers.remove(enabledSuppliers.size() - 1);
        enabledSuppliers.add(enabled);
        return this;
    }

    @Nonnull
    public TableDataSource buildDataSource(@Nullable String title)
    {
        return new TableDataSourceSupplied((Supplier<TableElement>) () -> buildElement(title));
    }

    @Nonnull
    public TableDataSource buildDataSource(@Nullable String title, List<String> tooltip)
    {
        return new TableDataSourceSupplied((Supplier<TableElement>) () -> buildElement(title).withTitleTooltip(tooltip));
    }

    @Nonnull
    public TableDataSource buildDataSource()
    {
        return new TableDataSourceSupplied(this::buildElement);
    }

    @Nonnull
    public TableElementCell buildElement(@Nullable String title)
    {
        return new TableElementCell(title, build());
    }

    @Nonnull
    public TableElementCell buildElement()
    {
        return new TableElementCell(build());
    }

    @Nonnull
    public TableCell build()
    {
        List<TableCell> cells = new ArrayList<>();

        for (int i = 0; i < this.titles.size(); i++)
        {
            TableCellButton cell = new TableCellButton("action." + i, "action." + i, this.titles.get(i).get());
            int finalI = i;
            cell.addAction(() -> actions.get(finalI).run());

            Supplier<List<String>> tooltip = tooltips.get(i);
            if (tooltip != null)
                cell.setTooltip(tooltip.get());
            BooleanSupplier enabledSupplier = enabledSuppliers.get(i);
            if (enabledSupplier != null)
                cell.setEnabled(enabledSupplier.getAsBoolean());

            cells.add(cell);
        }

        return new TableCellMulti(cells);
    }
}
