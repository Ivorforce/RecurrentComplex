/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import net.minecraft.util.ResourceLocation;

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
    protected final List<Supplier<TableCellButton>> cells = new ArrayList<>();
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

    @Nonnull
    private static TableCellButton defaultCell(Object title, Supplier<List<String>> tooltip, Runnable action)
    {
        TableCellButton cell = new TableCellButton(null, null, "");

        setVisuals(title, tooltip, cell);
        cell.addAction(action);

        return cell;
    }

    private static void setVisuals(Object title, Supplier<List<String>> tooltip, TableCellButton cell)
    {
        cell.title = "";
        cell.setTexture(null);

        if (title instanceof ResourceLocation)
            cell.setTexture((ResourceLocation) title);
        else if (title instanceof String)
            cell.title = (String) title;

        cell.setTooltip(tooltip != null ? tooltip.get() : null);
    }

    public TableCellMultiBuilder addAction(Supplier<Object> title, @Nullable Supplier<List<String>> tooltip, Runnable action)
    {
        cells.add(() -> defaultCell(title.get(), tooltip, () ->
        {
            action.run();
            delegate.reloadData();
        }));
        enabledSuppliers.add(null);
        return this;
    }

    public TableCellMultiBuilder addNavigation(Supplier<TableDataSource> dataSource, Supplier<Object> title, @Nullable Supplier<List<String>> tooltip)
    {
        cells.add(() ->
        {
            TableCellButton edit = TableCells.edit(true, navigator, delegate, dataSource);
            setVisuals(title.get(), tooltip, edit);
            return edit;
        });
        enabledSuppliers.add(null);
        return this;
    }

    public TableCellMultiBuilder addNavigation(Supplier<TableDataSource> dataSource, Supplier<Object> title)
    {
        return addNavigation(dataSource, title, null);
    }

    public TableCellMultiBuilder addNavigation(Supplier<TableDataSource> dataSource)
    {
        return addNavigation(dataSource, () -> IvTranslations.get("reccomplex.gui.edit"));
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
        return new TableDataSourceSupplied((Supplier<TableCell>) () -> buildTitled(title));
    }

    @Nonnull
    public TableDataSource buildDataSource(@Nullable String title, List<String> tooltip)
    {
        return new TableDataSourceSupplied((Supplier<TableCell>) () -> buildTitled(title).withTitleTooltip(tooltip));
    }

    @Nonnull
    public TableDataSource buildDataSource()
    {
        return new TableDataSourceSupplied(this::buildTitled);
    }

    @Nonnull
    public TitledCell buildTitled(@Nullable String title)
    {
        return new TitledCell(title, build());
    }

    @Nonnull
    public TitledCell buildTitled()
    {
        return new TitledCell(build());
    }

    @Nonnull
    public TableCellDefault build()
    {
        List<TableCellButton> cells = new ArrayList<>();

        for (int i = 0; i < this.cells.size(); i++)
        {
            TableCellButton cell = this.cells.get(i).get();

            cell.setId("action." + i);
            cell.actionID = "action." + i;

            BooleanSupplier enabled = enabledSuppliers.get(i);
            if (enabled != null)
                cell.setEnabled(enabled.getAsBoolean());

            cells.add(cell);
        }

        return new TableCellMulti(cells);
    }
}
