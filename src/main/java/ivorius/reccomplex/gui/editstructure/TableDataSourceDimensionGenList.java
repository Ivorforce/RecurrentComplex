/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceDimensionGenList extends TableDataSourceSegmented implements TableElementButton.Listener, TableElementPresetAction.Listener
{
    private List<DimensionGenerationInfo> dimensionGenerationInfos;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    public TableDataSourceDimensionGenList(List<DimensionGenerationInfo> dimensionGenerationInfos, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.dimensionGenerationInfos = dimensionGenerationInfos;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;
    }

    public List<DimensionGenerationInfo> getDimensionGenerationInfos()
    {
        return Collections.unmodifiableList(dimensionGenerationInfos);
    }

    public void setDimensionGenerationInfos(List<DimensionGenerationInfo> dimensionGenerationInfos)
    {
        this.dimensionGenerationInfos = dimensionGenerationInfos;
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
                return dimensionGenerationInfos.size();
            case 2:
                return 1;
        }

        return 0;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableElementPresetAction elementPresetAction = new TableElementPresetAction("dimensionPreset", "Presets", "Apply", new TableElementButton.Action("overworld", "Overworld"), new TableElementButton.Action("nether", "Nether"), new TableElementButton.Action("end", "End"), new TableElementButton.Action("clear", "Clear"));
            elementPresetAction.addListener(this);
            return elementPresetAction;
        }
        else if (segment == 1)
        {
            int dimGenIndex = index;
            TableElementButton.Action[] actions = {new TableElementButton.Action("earlier", "Earlier", dimGenIndex > 0), new TableElementButton.Action("later", "Later", dimGenIndex < dimensionGenerationInfos.size() - 1), new TableElementButton.Action("edit", "Edit"), new TableElementButton.Action("delete", "Delete")};
            DimensionGenerationInfo generationInfo = dimensionGenerationInfos.get(dimGenIndex);

            String title = StringUtils.abbreviate(generationInfo.getDisplayString(), 16) + " (" + generationInfo.getActiveGenerationWeight() + ")";
            TableElementButton button = new TableElementButton("dimensionGen" + dimGenIndex, title, actions);
            button.addListener(this);
            return button;
        }
        else if (segment == 2)
        {
            TableElementButton addButton = new TableElementButton("addGen", "Add", new TableElementButton.Action("addGen", "Add Dimension"));
            addButton.addListener(this);
            return addButton;
        }

        return null;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if (actionID.equals("addGen"))
        {
            DimensionGenerationInfo generationInfo = new DimensionGenerationInfo("", null);
            dimensionGenerationInfos.add(generationInfo);
            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceDimensionGen(generationInfo, tableDelegate)));
        }
        else if (tableElementButton.getID().startsWith("dimensionGen"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(12));
            DimensionGenerationInfo generationInfo = dimensionGenerationInfos.get(index);

            switch (actionID)
            {
                case "edit":
                    navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceDimensionGen(generationInfo, tableDelegate)));
                    break;
                case "delete":
                    dimensionGenerationInfos.remove(generationInfo);
                    tableDelegate.reloadData();
                    break;
                case "earlier":
                    dimensionGenerationInfos.remove(index);
                    dimensionGenerationInfos.add(index - 1, generationInfo);
                    tableDelegate.reloadData();
                    break;
                case "later":
                    dimensionGenerationInfos.remove(index);
                    dimensionGenerationInfos.add(index + 1, generationInfo);
                    tableDelegate.reloadData();
                    break;
            }
        }
    }

    @Override
    public void actionPerformed(TableElementPresetAction tableElementButton, String actionID)
    {
        if (tableElementButton.getID().equals("dimensionPreset"))
        {
            switch (actionID)
            {
                case "overworld":
                    dimensionGenerationInfos.clear();
                    dimensionGenerationInfos.addAll(DimensionGenerationInfo.overworldGenerationList());
                    break;
                case "nether":
                    dimensionGenerationInfos.clear();
                    dimensionGenerationInfos.addAll(DimensionGenerationInfo.netherGenerationList());
                    break;
                case "end":
                    dimensionGenerationInfos.clear();
                    dimensionGenerationInfos.addAll(DimensionGenerationInfo.endGenerationList());
                    break;
                case "clear":
                    dimensionGenerationInfos.clear();
                    break;
            }

            tableDelegate.reloadData();
        }
    }
}
