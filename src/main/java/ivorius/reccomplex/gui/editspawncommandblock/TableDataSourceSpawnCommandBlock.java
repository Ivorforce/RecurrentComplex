/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editspawncommandblock;

import ivorius.reccomplex.blocks.TileEntitySpawnCommand;
import ivorius.reccomplex.gui.table.*;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceSpawnCommandBlock extends TableDataSourceSegmented implements TableElementButton.Listener
{
    private TileEntitySpawnCommand tileEntity;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    public TableDataSourceSpawnCommandBlock(TileEntitySpawnCommand tileEntity, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.tileEntity = tileEntity;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;
    }

    public TileEntitySpawnCommand getTileEntity()
    {
        return tileEntity;
    }

    public void setTileEntity(TileEntitySpawnCommand tileEntity)
    {
        this.tileEntity = tileEntity;
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

    @Override
    public int numberOfSegments()
    {
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 ? 1 : tileEntity.getEntries().size();
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableElementButton addButton = new TableElementButton("addCommand", "Add", new TableElementButton.Action("addCommand", "Add Command"));
            addButton.addListener(this);
            return addButton;
        }
        else if (segment == 1)
        {
            int listIndex = index;
            TableElementButton.Action[] actions = {new TableElementButton.Action("earlier", "Up", listIndex > 0), new TableElementButton.Action("later", "Down", listIndex < tileEntity.entries.size() - 1), new TableElementButton.Action("edit", "Edit"), new TableElementButton.Action("delete", "Delete")};
            TileEntitySpawnCommand.Entry entry = tileEntity.entries.get(listIndex);

            String title = StringUtils.abbreviate(entry.command, 16) + " (" + entry.itemWeight + ")";
            TableElementButton button = new TableElementButton("command" + listIndex, title, actions);
            button.addListener(this);
            return button;
        }

        return null;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if (actionID.equals("addCommand"))
        {
            TileEntitySpawnCommand.Entry entry = new TileEntitySpawnCommand.Entry(100, "");
            tileEntity.entries.add(entry);
            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceSpawnCommandEntry(entry, tableDelegate)));
        }
        else if (tableElementButton.getID().startsWith("command"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring("command".length()));
            TileEntitySpawnCommand.Entry entry = tileEntity.entries.get(index);

            switch (actionID)
            {
                case "edit":
                    navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceSpawnCommandEntry(entry, tableDelegate)));
                    break;
                case "delete":
                    tileEntity.entries.remove(entry);
                    tableDelegate.reloadData();
                    break;
                case "earlier":
                    tileEntity.entries.remove(index);
                    tileEntity.entries.add(index - 1, entry);
                    tableDelegate.reloadData();
                    break;
                case "later":
                    tileEntity.entries.remove(index);
                    tileEntity.entries.add(index + 1, entry);
                    tableDelegate.reloadData();
                    break;
            }
        }
    }
}
