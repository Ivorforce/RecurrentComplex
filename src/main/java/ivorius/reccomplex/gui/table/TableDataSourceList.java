/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import com.mojang.realmsclient.gui.ChatFormatting;

import java.util.List;

/**
 * Created by lukas on 20.02.15.
 */
public abstract class TableDataSourceList<T, L extends List<T>> extends TableDataSourceSegmented implements TableCellActionListener
{
    protected L list;

    protected TableDelegate tableDelegate;
    protected TableNavigator navigator;

    protected String earlierTitle = "Earlier";
    protected String laterTitle = "Later";
    protected String editTitle = "Edit";
    protected String deleteTitle = ChatFormatting.RED + "-";
    protected String addTitle = ChatFormatting.GREEN + "+";

    protected boolean usesPresetActionForAdding;

    public TableDataSourceList(L list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.list = list;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;
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
            return list.size();

        int addIndex = getAddIndex(segment);
        if (addIndex >= 0)
            return 1;

        return 0;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (isListSegment(segment))
        {
            T t = list.get(index);

            TableCellButton cell = new TableCellButton("entry" + index, getEntryActions(index));
            cell.addListener(this);
            return new TableElementCell(getDisplayString(t), cell);
        }

        int addIndex = getAddIndex(segment);
        if (addIndex >= 0)
        {
            if (isUsesPresetActionForAdding())
            {
                TableCellPresetAction cell = new TableCellPresetAction("add" + addIndex, getAddTitle(), getAddActions());
                cell.setActionButtonWidth(0.2f);
                cell.addListener(this);
                return new TableElementCell(cell);
            }
            else
            {
                TableCellButton cell = new TableCellButton("add" + addIndex, getAddActions());
                cell.addListener(this);
                return new TableElementCell(cell);
            }
        }

        return null;
    }

    public boolean isListSegment(int segment)
    {
        return segment == 1;
    }

    public int getAddIndex(int segment)
    {
        return segment == 0
                ? 0
                : segment == 2
                ? (list.size() > 0 ? list.size() : -1)
                : -1;
    }

    public TableCellButton.Action[] getAddActions()
    {
        boolean enabled = canEditList();
        return new TableCellButton.Action[]{new TableCellButton.Action("add", getAddTitle(), enabled)};
    }

    public TableCellButton.Action[] getEntryActions(int index)
    {
        boolean enabled = canEditList();
        return new TableCellButton.Action[]{
                new TableCellButton.Action("earlier", getEarlierTitle(), index > 0 && enabled),
                new TableCellButton.Action("later", getLaterTitle(), index < list.size() - 1 && enabled),
                new TableCellButton.Action("edit", getEditTitle(), enabled),
                new TableCellButton.Action("delete", getDeleteTitle(), enabled)
        };
    }

    public boolean canEditList()
    {
        return true;
    }

    @Override
    public void actionPerformed(TableCell cell, String actionID)
    {
        if (cell.getID() != null)
        {
            if (cell.getID().startsWith("add"))
            {
                T entry = newEntry(actionID);
                if (entry != null)
                {
                    int addIndex = Integer.valueOf(cell.getID().substring("add".length()));
                    list.add(addIndex, entry);

                    navigator.pushTable(new GuiTable(tableDelegate, editEntryDataSource(entry)));
                }
            }
            else if (cell.getID().startsWith("entry"))
            {
                int index = Integer.valueOf(cell.getID().substring("entry".length()));
                T entry = list.get(index);

                performEntryAction(actionID, index, entry);
            }
        }
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
