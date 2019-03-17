/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import gnu.trove.list.TFloatList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Created by lukas on 27.08.16.
 */

@SideOnly(Side.CLIENT)
public class TableCellMultiBuilder
{
    protected final List<Supplier<TableCellDefault>> cells = new ArrayList<>();
    protected final List<BooleanSupplier> enabledSuppliers = new ArrayList<>();
    protected final TIntObjectMap<Supplier<Float>> sizers = new TIntObjectHashMap<>();

    public TableNavigator navigator;
    public TableDelegate delegate;

    public String title;
    public List<String> titleTooltip;

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
        cell.setTitle("");
        cell.setTexture(null);

        if (title instanceof ResourceLocation)
            cell.setTexture((ResourceLocation) title);
        else if (title instanceof String)
            cell.setTitle((String) title);

        cell.setTooltip(tooltip != null ? tooltip.get() : null);
    }

    @Nonnull
    public TableCellMultiBuilder addCell(Supplier<TableCellDefault> cell)
    {
        cells.add(cell);
        enabledSuppliers.add(null);
        return this;
    }

    public TableCellMultiBuilder addAction(Runnable action, Supplier<Object> title, @Nullable Supplier<List<String>> tooltip)
    {
        return addCell(() -> defaultCell(title.get(), tooltip, () ->
        {
            action.run();
            delegate.reloadData();
        }));
    }

    public TableCellMultiBuilder addSimpleNavigation(Supplier<TableDataSource> dataSource, Supplier<Object> title, @Nullable Supplier<List<String>> tooltip)
    {
        return addCell(() -> defaultCell(title.get(), tooltip, () -> navigator.pushTable(new GuiTable(delegate, dataSource.get()))));
    }

    public TableCellMultiBuilder addNavigation(Supplier<TableDataSource> dataSource, Supplier<Object> title, @Nullable Supplier<List<String>> tooltip)
    {
        return addCell(() ->
        {
            TableCellButton edit = TableCells.edit(true, navigator, delegate, dataSource);
            setVisuals(title.get(), tooltip, edit);
            return edit;
        });
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

    public TableCellMultiBuilder sized(Supplier<Float> sizer)
    {
        sizers.put(cells.size() - 1, sizer);
        return this;
    }

    @Nonnull
    public TableDataSource buildDataSource()
    {
        return new TableDataSourceSupplied(this::build);
    }

    @Nonnull
    public TableCellMultiBuilder withTitle(@Nullable String title)
    {
        this.title = title;
        return this;
    }

    @Nonnull
    public TableCellMultiBuilder withTitle(@Nullable String title, List<String> tooltip)
    {
        this.title = title;
        this.titleTooltip = tooltip;
        return this;
    }

    @Nonnull
    public TableCellDefault build()
    {
        List<TableCellDefault> cells = new ArrayList<>();

        for (int i = 0; i < this.cells.size(); i++) {
            TableCellDefault cell = this.cells.get(i).get();

//            cell.setId("action." + i);
//            cell.actionID = "action." + i;

            BooleanSupplier enabled = enabledSuppliers.get(i);
            if (enabled != null)
                cell.setEnabled(enabled.getAsBoolean());

            cells.add(cell);
        }

        TableCellMulti multi = new TableCellMulti(cells);

        sizers.forEachEntry((idx, supplier) -> {
            multi.setSize(idx, supplier.get());
            return true;
        });

        return title != null ? new TitledCell(title, multi).withTitleTooltip(titleTooltip) : multi;
    }
}
