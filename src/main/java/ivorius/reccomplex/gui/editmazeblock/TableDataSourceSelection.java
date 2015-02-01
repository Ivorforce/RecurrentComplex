/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.reccomplex.gui.editstructure.TableDataSourceBiomeGen;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.worldgen.genericStructures.Selection;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceSelection extends TableDataSourceSegmented implements TableElementButton.Listener
{
    private Selection selection;
    private int[] dimensions;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    public TableDataSourceSelection(Selection selection, int[] dimensions, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.selection = selection;
        this.dimensions = dimensions;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;
    }

    public Selection getSelection()
    {
        return selection;
    }

    public void setSelection(Selection selection)
    {
        this.selection = selection;
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
        return 3;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
                return 1;
            case 1:
                return selection.size();
            case 2:
                return selection.size() > 0 ? 1 : 0;
        }

        return 0;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 1)
        {
            Selection.Area area = selection.get(index);

            EnumChatFormatting color = area.isAdditive() ? EnumChatFormatting.GREEN : EnumChatFormatting.RED;
            String title = String.format(color + "%s - %s", Arrays.toString(area.getMinCoord()), Arrays.toString(area.getMaxCoord()));

            TableElementButton.Action[] actions = {new TableElementButton.Action("earlier", "Earlier", index > 0), new TableElementButton.Action("later", "Later", index < selection.size() - 1), new TableElementButton.Action("edit", "Edit"), new TableElementButton.Action("delete", "Delete")};
            TableElementButton button = new TableElementButton("area" + index, title, actions);
            button.addListener(this);
            return button;
        }
        else if (segment == 0 || segment == 2)
        {
            TableElementButton addButton = new TableElementButton("addArea", "Add", new TableElementButton.Action(segment == 0 ? "addAreaB" : "addAreaE", "Add Area"));
            addButton.addListener(this);
            return addButton;
        }

        return null;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if (actionID.startsWith("addArea"))
        {
            Selection.Area area = new Selection.Area(true, new int[dimensions.length], new int[dimensions.length]);

            if (actionID.endsWith("B"))
                selection.add(0, area);
            else
                selection.add(area);

            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceSelectionArea(area, dimensions)));
        }
        else if (tableElementButton.getID().startsWith("area"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(4));
            Selection.Area area = selection.get(index);

            switch (actionID)
            {
                case "edit":
                    navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceSelectionArea(area, dimensions)));
                    break;
                case "delete":
                    selection.remove(area);
                    tableDelegate.reloadData();
                    break;
                case "earlier":
                    selection.remove(index);
                    selection.add(index - 1, area);
                    tableDelegate.reloadData();
                    break;
                case "later":
                    selection.remove(index);
                    selection.add(index + 1, area);
                    tableDelegate.reloadData();
                    break;
            }
        }
    }

}
