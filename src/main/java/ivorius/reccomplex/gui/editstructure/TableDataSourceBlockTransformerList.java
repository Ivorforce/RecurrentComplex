/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.blocktransformers.BlockTransformer;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceBlockTransformerList extends TableDataSourceSegmented implements TableElementButton.Listener, TableElementPresetAction.Listener
{
    private List<BlockTransformer> blockTransformerList;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    public TableDataSourceBlockTransformerList(List<BlockTransformer> blockTransformerList, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.blockTransformerList = blockTransformerList;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;
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

    @Override
    public int numberOfSegments()
    {
        return 3;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 || segment == 2 ? 1 : blockTransformerList.size();
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0 || segment == 2)
        {
            Collection<String> allTypes = StructureRegistry.getBlockTransformerRegistry().allIDs();
            List<TableElementButton.Action> actions = new ArrayList<>(allTypes.size());
            for (String type : allTypes)
                actions.add(new TableElementButton.Action(type, StatCollector.translateToLocal("reccomplex.blockTransformer." + type)));

            int addIndex = segment == 0 ? 0 : blockTransformerList.size();
            TableElementPresetAction addButton = new TableElementPresetAction("add" + addIndex, "Type", "Add", actions.toArray(new TableElementButton.Action[actions.size()]));
            addButton.addListener(this);
            return addButton;
        }
        else if (segment == 1)
        {
            TableElementButton.Action[] actions = {new TableElementButton.Action("earlier", "Earlier", index > 0), new TableElementButton.Action("later", "Later", index < blockTransformerList.size() - 1), new TableElementButton.Action("edit", "Edit"), new TableElementButton.Action("delete", "Delete")};
            TableElementButton button = new TableElementButton("transformer" + index, blockTransformerList.get(index).displayString(), actions);
            button.addListener(this);
            return button;
        }

        return null;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if (tableElementButton.getID().startsWith("transformer"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(11));
            BlockTransformer blockTransformer = blockTransformerList.get(index);

            switch (actionID)
            {
                case "edit":
                    navigator.pushTable(new GuiTable(tableDelegate, blockTransformer.tableDataSource(navigator, tableDelegate)));
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

    @Override
    public void actionPerformed(TableElementPresetAction tableElementButton, String actionID)
    {
        if (tableElementButton.getID().startsWith("add"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(3));
            Class<? extends BlockTransformer> clazz = StructureRegistry.getBlockTransformerRegistry().typeForID(actionID);

            BlockTransformer blockTransformer = instantiateBlockTransformer(clazz);
            if (blockTransformer != null)
            {
                TableDataSource tableDataSource = blockTransformer.tableDataSource(navigator, tableDelegate);

                blockTransformerList.add(index, blockTransformer);
                navigator.pushTable(new GuiTable(tableDelegate, tableDataSource));
            }
        }
    }

    public BlockTransformer instantiateBlockTransformer(Class<? extends BlockTransformer> clazz)
    {
        BlockTransformer blockTransformer = null;

        try
        {
            blockTransformer = clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            RecurrentComplex.logger.error(e);
        }

        return blockTransformer;
    }
}
