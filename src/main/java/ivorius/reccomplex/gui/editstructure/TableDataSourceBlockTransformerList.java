/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.worldgen.StructureHandler;
import ivorius.reccomplex.worldgen.blockTransformers.BlockTransformer;
import ivorius.reccomplex.worldgen.blockTransformers.BlockTransformerProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceBlockTransformerList implements TableDataSource, TableElementButton.Listener
{
    private List<BlockTransformer> blockTransformerList;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    private String currentTransformerType;

    public TableDataSourceBlockTransformerList(List<BlockTransformer> blockTransformerList, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.blockTransformerList = blockTransformerList;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;
        currentTransformerType = nextTransformerID(null);
    }

    public List<BlockTransformer> getBlockTransformerList()
    {
        return Collections.unmodifiableList(blockTransformerList);
    }

    public void setBlockTransformerList(List<BlockTransformer> blockTransformerList)
    {
        this.blockTransformerList = blockTransformerList;
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

    private static String nextTransformerID(String transformerID)
    {
        Collection<String> allTypes = StructureHandler.allBlockTransformerIDs();
        String[] allTypesArray = allTypes.toArray(new String[allTypes.size()]);

        for (int i = 0; i < allTypesArray.length; i++)
        {
            if (allTypesArray[i].equals(transformerID))
            {
                return allTypesArray[(i + 1) % allTypesArray.length];
            }
        }

        return allTypesArray[0];
    }

    @Override
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index < blockTransformerList.size() + 2;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index == 0 || index == blockTransformerList.size() + 1)
        {
            int realIndex = index == 0 ? index : index - 1;
            TableElementButton addButton = new TableElementButton("add" + realIndex, "Type", new TableElementButton.Action("changeType", currentTransformerType), new TableElementButton.Action("add", "Add"));
            addButton.addListener(this);
            return addButton;
        }

        int transformerIndex = index - 1;
        TableElementButton.Action[] actions = {new TableElementButton.Action("earlier", "Earlier", transformerIndex > 0), new TableElementButton.Action("later", "Later", transformerIndex < blockTransformerList.size() - 1), new TableElementButton.Action("edit", "Edit"), new TableElementButton.Action("delete", "Delete")};
        TableElementButton button = new TableElementButton("transformer" + transformerIndex, blockTransformerList.get(transformerIndex).displayString(), actions);
        button.addListener(this);
        return button;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if (actionID.equals("add"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(3));
            BlockTransformerProvider provider = StructureHandler.blockTransformerProviderForID(currentTransformerType);

            BlockTransformer blockTransformer = provider.defaultTransformer();
            TableDataSource tableDataSource = provider.tableDataSource(blockTransformer);

            blockTransformerList.add(index, blockTransformer);
            navigator.pushTable(new GuiTable(tableDelegate, tableDataSource));
        }
        else if (actionID.equals("changeType"))
        {
            currentTransformerType = nextTransformerID(currentTransformerType);
            tableDelegate.reloadData();
        }
        else if (tableElementButton.getID().startsWith("transformer"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(11));
            BlockTransformer blockTransformer = blockTransformerList.get(index);
            BlockTransformerProvider provider = StructureHandler.blockTransformerProviderForID(StructureHandler.blockTransformerIDForType(blockTransformer.getClass()));

            switch (actionID)
            {
                case "edit":
                    navigator.pushTable(new GuiTable(tableDelegate, provider.tableDataSource(blockTransformer)));
                    break;
                case "delete":
                    blockTransformerList.remove(blockTransformer);
                    tableDelegate.reloadData();
                    break;
                case "earlier":
                    blockTransformerList.remove(index);
                    blockTransformerList.add(index - 1, blockTransformer);
                    tableDelegate.reloadData();
                    break;
                case "later":
                    blockTransformerList.remove(index);
                    blockTransformerList.add(index + 1, blockTransformer);
                    tableDelegate.reloadData();
                    break;
            }
        }
    }
}
