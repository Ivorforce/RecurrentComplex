/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.datasource;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.utils.IvClasses;
import net.minecraft.util.text.TextFormatting;
import ivorius.ivtoolkit.tools.IvTranslations;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 20.02.15.
 */
public abstract class TableDataSourceList<T, L extends List<T>> extends TableDataSourceSegmented
{
    protected L list;

    protected TableDelegate tableDelegate;
    protected TableNavigator navigator;

    protected String earlierTitle = TextFormatting.BOLD + "↑";
    protected String laterTitle = TextFormatting.BOLD + "↓";
    protected String editTitle = IvTranslations.get("reccomplex.gui.edit");
    protected String deleteTitle = TextFormatting.RED + "X";
    protected String addTitle = TextFormatting.GREEN + "+";

    protected boolean usesPresetActionForAdding;

    public TableDataSourceList(L list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.list = list;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;
    }

    @Nullable
    protected T tryInstantiate(String actionID, Class<? extends T> clazz, String format)
    {
        if (clazz == null)
        {
            RecurrentComplex.logger.error(String.format(format, actionID));
            return null;
        }
        return IvClasses.instantiate(clazz);
    }

    public L getList()
    {
        return list;
    }

    public void setList(L list)
    {
        this.list = list;
    }

    public TableDelegate getTableDelegate()
    {
        return tableDelegate;
    }

    public void setTableDelegate(TableDelegate tableDelegate)
    {
        this.tableDelegate = tableDelegate;
    }

    public TableNavigator getNavigator()
    {
        return navigator;
    }

    public void setNavigator(TableNavigator navigator)
    {
        this.navigator = navigator;
    }

    public String getEarlierTitle()
    {
        return earlierTitle;
    }

    public void setEarlierTitle(String earlierTitle)
    {
        this.earlierTitle = earlierTitle;
    }

    public String getLaterTitle()
    {
        return laterTitle;
    }

    public void setLaterTitle(String laterTitle)
    {
        this.laterTitle = laterTitle;
    }

    public String getEditTitle()
    {
        return editTitle;
    }

    public void setEditTitle(String editTitle)
    {
        this.editTitle = editTitle;
    }

    public String getDeleteTitle()
    {
        return deleteTitle;
    }

    public void setDeleteTitle(String deleteTitle)
    {
        this.deleteTitle = deleteTitle;
    }

    public String getAddTitle()
    {
        return addTitle;
    }

    public void setAddTitle(String addTitle)
    {
        this.addTitle = addTitle;
    }

    public boolean isUsesPresetActionForAdding()
    {
        return usesPresetActionForAdding;
    }

    public void setUsesPresetActionForAdding(boolean usesPresetActionForAdding)
    {
        this.usesPresetActionForAdding = usesPresetActionForAdding;
    }

    @Override
    public int numberOfSegments()
    {
        return 3;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        if (isListSegment(segment))
            return Math.max(list.size(), 1);

        int addIndex = getAddIndex(segment);
        if (addIndex >= 0)
            return 1;

        return 0;
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (isListSegment(segment))
        {
            if (list.size() == 0)
                return new TitledCell(new TableCellTitle(null, String.format("%s%s%s", TextFormatting.GRAY, TextFormatting.ITALIC, IvTranslations.get("reccomplex.gui.list.noelements"))));

            T t = list.get(index);

            TableCellButton[] cells = getEntryActions(index);
            for (TableCellButton cell : cells)
            {
                cell.addAction(() -> {
                    T entry = list.get(index);
                    performEntryAction(cell.actionID, index, entry);
                });
                cell.setId("entry" + index);
            }
            TableCellMulti multi = new TableCellMulti(cells);
            multi.setSize(0, 8);
            return new TitledCell(getDisplayString(t), multi);
        }

        int addIndex = getAddIndex(segment);
        if (addIndex >= 0)
        {
            if (isUsesPresetActionForAdding())
            {
                TableCellPresetAction cell = new TableCellPresetAction("add" + addIndex, getAddActions());
                cell.addAction(actionID -> createAddAction(addIndex, actionID).run());
                return new TitledCell(cell);
            }
            else
            {
                List<TableCellButton> cells = getAddActions();
                for (TableCellButton cell : cells)
                {
                    cell.addAction(createAddAction(addIndex, cell.actionID));
                    cell.setId("add" + addIndex);
                }
                return new TitledCell(new TableCellMulti(cells));
            }
        }

        return null;
    }

    @Nonnull
    protected Runnable createAddAction(int addIndex, String actionID)
    {
        return () -> {
            T entry = newEntry(actionID);
            if (entry != null)
            {
                list.add(addIndex, entry);
                navigator.pushTable(new GuiTable(tableDelegate, editEntryDataSource(entry)));
            }
        };
    }

    public boolean isListSegment(int segment)
    {
        return segment == 1;
    }

    public int getAddIndex(int segment)
    {
        return segment == 0 ? 0
                : segment == 2 ? list.size()
                : -1;
    }

    public List<TableCellButton> getAddActions()
    {
        boolean enabled = canEditList();
        return Collections.singletonList(new TableCellButton("", "add", getAddTitle(), enabled));
    }

    public TableCellButton[] getEntryActions(int index)
    {
        boolean enabled = canEditList();
        return new TableCellButton[]{
                new TableCellButton("", "edit", getEditTitle(), enabled),
                new TableCellButton("", "earlier", getEarlierTitle(), index > 0 && enabled),
                new TableCellButton("", "later", getLaterTitle(), index < list.size() - 1 && enabled),
                new TableCellButton("", "delete", getDeleteTitle(), enabled)
        };
    }

    public boolean canEditList()
    {
        return true;
    }

    public void performEntryAction(String actionID, int index, T t)
    {
        switch (actionID)
        {
            case "edit":
                navigator.pushTable(new GuiTable(tableDelegate, editEntryDataSource(t)));
                break;
            case "delete":
                list.remove(index);
                tableDelegate.reloadData();
                break;
            case "earlier":
                list.remove(index);
                list.add(index - 1, t);
                tableDelegate.reloadData();
                break;
            case "later":
                list.remove(index);
                list.add(index + 1, t);
                tableDelegate.reloadData();
                break;
        }
    }

    public abstract String getDisplayString(T t);

    public abstract T newEntry(String actionID);

    public abstract TableDataSource editEntryDataSource(T t);
}
