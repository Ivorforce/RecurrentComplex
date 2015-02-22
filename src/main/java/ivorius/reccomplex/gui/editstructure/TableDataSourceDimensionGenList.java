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
public class TableDataSourceDimensionGenList extends TableDataSourceList<DimensionGenerationInfo, List<DimensionGenerationInfo>> implements TableElementActionListener
{
    public TableDataSourceDimensionGenList(List<DimensionGenerationInfo> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        setAddTitle("Add Dimension");
    }

    @Override
    public int numberOfSegments()
    {
        return super.numberOfSegments() + 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 ? 1 : super.sizeOfSegment(segment);
    }

    @Override
    public boolean isListSegment(int segment)
    {
        return segment == 2;
    }

    @Override
    public int getAddIndex(int segment)
    {
        return segment == 1
                ? 0
                : segment == 3
                ? (list.size() > 0 ? list.size() : -1)
                : -1;
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

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public String getDisplayString(DimensionGenerationInfo generationInfo)
    {
        return StringUtils.abbreviate(generationInfo.getDisplayString(), 16) + " (" + generationInfo.getActiveGenerationWeight() + ")";
    }

    @Override
    public DimensionGenerationInfo newEntry(String actionID)
    {
        return new DimensionGenerationInfo("", null);
    }

    @Override
    public TableDataSource editEntryDataSource(DimensionGenerationInfo entry)
    {
        return new TableDataSourceDimensionGen(entry, tableDelegate);
    }

    @Override
    public void actionPerformed(TableElement element, String actionID)
    {
        if (element.getID().equals("dimensionPreset"))
        {
            switch (actionID)
            {
                case "overworld":
                    list.clear();
                    list.addAll(DimensionGenerationInfo.overworldGenerationList());
                    break;
                case "nether":
                    list.clear();
                    list.addAll(DimensionGenerationInfo.netherGenerationList());
                    break;
                case "end":
                    list.clear();
                    list.addAll(DimensionGenerationInfo.endGenerationList());
                    break;
                case "clear":
                    list.clear();
                    break;
            }

            tableDelegate.reloadData();
        }

        super.actionPerformed(element, actionID);
    }
}
