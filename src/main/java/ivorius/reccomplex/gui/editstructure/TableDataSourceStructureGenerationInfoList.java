/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.json.SerializableStringTypeRegistry;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.blocktransformers.BlockTransformer;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceStructureGenerationInfoList extends TableDataSourceSegmented implements TableElementButton.Listener, TableElementPresetAction.Listener
{
    private List<StructureGenerationInfo> structureGenerationInfos;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    public TableDataSourceStructureGenerationInfoList(List<StructureGenerationInfo> structureGenerationInfos, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.structureGenerationInfos = structureGenerationInfos;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;
    }

    public List<StructureGenerationInfo> getStructureGenerationInfos()
    {
        return Collections.unmodifiableList(structureGenerationInfos);
    }

    public void setStructureGenerationInfos(List<StructureGenerationInfo> structureGenerationInfos)
    {
        this.structureGenerationInfos = structureGenerationInfos;
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
        return segment == 0 || segment == 2 ? 1 : structureGenerationInfos.size();
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0 || segment == 2)
        {
            Collection<String> allTypes = StructureRegistry.getStructureGenerationInfoRegistry().allIDs();
            List<TableElementButton.Action> actions = new ArrayList<>(allTypes.size());
            for (String type : allTypes)
                actions.add(new TableElementButton.Action(type, StatCollector.translateToLocal("reccomplex.generationInfo." + type)));

            int addIndex = segment == 0 ? 0 : structureGenerationInfos.size();
            TableElementPresetAction addButton = new TableElementPresetAction("add" + addIndex, "Type", "Add", actions.toArray(new TableElementButton.Action[actions.size()]));
            addButton.addListener(this);
            return addButton;
        }
        else if (segment == 1)
        {
            TableElementButton.Action[] actions = {new TableElementButton.Action("earlier", "Up", index > 0), new TableElementButton.Action("later", "Down", index < structureGenerationInfos.size() - 1), new TableElementButton.Action("edit", "Edit"), new TableElementButton.Action("delete", "Delete")};
            TableElementButton button = new TableElementButton("generationInfo" + index, structureGenerationInfos.get(index).displayString(), actions);
            button.addListener(this);
            return button;
        }

        return null;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if (tableElementButton.getID().startsWith("generationInfo"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring("generationInfo".length()));
            StructureGenerationInfo generationInfo = structureGenerationInfos.get(index);

            switch (actionID)
            {
                case "edit":
                    navigator.pushTable(new GuiTable(tableDelegate, generationInfo.tableDataSource(navigator, tableDelegate)));
                    break;
                case "delete":
                    structureGenerationInfos.remove(generationInfo);
                    tableDelegate.reloadData();
                    break;
                case "earlier":
                    structureGenerationInfos.remove(index);
                    structureGenerationInfos.add(index - 1, generationInfo);
                    tableDelegate.reloadData();
                    break;
                case "later":
                    structureGenerationInfos.remove(index);
                    structureGenerationInfos.add(index + 1, generationInfo);
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
            Class<? extends StructureGenerationInfo> clazz = StructureRegistry.getStructureGenerationInfoRegistry().typeForID(actionID);

            StructureGenerationInfo generationInfo = instantiateStructureGenerationInfo(clazz);
            TableDataSource tableDataSource = generationInfo.tableDataSource(navigator, tableDelegate);

            structureGenerationInfos.add(index, generationInfo);
            navigator.pushTable(new GuiTable(tableDelegate, tableDataSource));
        }
    }

    public StructureGenerationInfo instantiateStructureGenerationInfo(Class<? extends StructureGenerationInfo> clazz)
    {
        StructureGenerationInfo generationInfo = null;

        try
        {
            generationInfo = clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            RecurrentComplex.logger.error(e);
        }

        return generationInfo;
    }
}
