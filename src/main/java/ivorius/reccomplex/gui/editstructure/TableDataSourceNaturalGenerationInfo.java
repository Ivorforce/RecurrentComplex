/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.worldgen.StructureInfo;
import ivorius.reccomplex.worldgen.StructureSelector;
import ivorius.reccomplex.worldgen.genericStructures.GenerationYSelector;
import ivorius.reccomplex.worldgen.genericStructures.GenericStructureInfo;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.NaturalGenerationInfo;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceNaturalGenerationInfo implements TableDataSource, TableElementButton.Listener, TableElementPropertyListener
{
    private static List<TableElementList.Option> allGenerationOptions()
    {
        List<TableElementList.Option> generationBases = new ArrayList<>();

        for (GenerationYSelector.SelectionMode selectionMode : GenerationYSelector.SelectionMode.values())
        {
            generationBases.add(new TableElementList.Option(selectionMode.serializedName(), I18n.format("structures.genY." + selectionMode.serializedName())));
        }

        return generationBases;
    }

    private static List<TableElementList.Option> allGenerationCategories()
    {
        Set<String> categories = StructureSelector.allCategoryIDs();
        List<TableElementList.Option> generationBases = new ArrayList<>();

        for (String category : categories)
        {
            StructureSelector.Category categoryObj = StructureSelector.categoryForID(category);

            if (categoryObj.selectableInGUI())
                generationBases.add(new TableElementList.Option(category, I18n.format("structures.category." + category)));
        }

        return generationBases;
    }

    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private GenericStructureInfo structureInfo;

    public TableDataSourceNaturalGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, GenericStructureInfo structureInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.structureInfo = structureInfo;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if ("enableGen".equals(tableElementButton.getID()) && "toggle".equals(actionID))
        {
            if (structureInfo.naturalGenerationInfo != null)
                structureInfo.naturalGenerationInfo = null;
            else
                structureInfo.naturalGenerationInfo = new NaturalGenerationInfo("decoration", new GenerationYSelector(GenerationYSelector.SelectionMode.SURFACE, 0, 0));

            tableDelegate.reloadData();
        }
        else if ("editBiomes".equals(tableElementButton.getID()) && "edit".equals(actionID))
        {
            GuiTable editBiomesProperties = new GuiTable(tableDelegate, new TableDataSourceBiomeGenList(structureInfo.naturalGenerationInfo.generationWeights, tableDelegate, navigator));
            navigator.pushTable(editBiomesProperties);
        }
    }

    @Override
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index < (structureInfo.naturalGenerationInfo != null ? 5 : 1);
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index == 0)
        {
            TableElementButton element = new TableElementButton("enableGen", "Natural Generation", new TableElementButton.Action("toggle", structureInfo.naturalGenerationInfo != null ? "Delete" : "Enable"));
            element.addListener(this);
            return element;
        }
        else if (index == 1)
        {
            TableElementList element = new TableElementList("category", "Category", structureInfo.naturalGenerationInfo.generationCategory, allGenerationCategories());
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 2)
        {
            TableElementList element = new TableElementList("ySelType", "Generation Base", structureInfo.naturalGenerationInfo.ySelector.selectionMode.serializedName(), allGenerationOptions());
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 3)
        {
            TableElementIntegerRange element = new TableElementIntegerRange("ySelShift", "Y Shift", new IntegerRange(structureInfo.naturalGenerationInfo.ySelector.minY, structureInfo.naturalGenerationInfo.ySelector.maxY), -100, 100);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 4)
        {
            TableElementButton elementEditBiomes = new TableElementButton("editBiomes", "Biomes", new TableElementButton.Action("edit", "Edit"));
            elementEditBiomes.addListener(this);
            return elementEditBiomes;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("category".equals(element.getID()))
        {
            structureInfo.naturalGenerationInfo.generationCategory = (String) element.getPropertyValue();
        }
        else if ("ySelType".equals(element.getID()))
        {
            GenerationYSelector.SelectionMode selectionMode = GenerationYSelector.SelectionMode.selectionMode((String) element.getPropertyValue());
            structureInfo.naturalGenerationInfo.ySelector.selectionMode = selectionMode != null ? selectionMode : GenerationYSelector.SelectionMode.SURFACE;
        }
        else if ("ySelShift".equals(element.getID()))
        {
            IntegerRange range = ((IntegerRange) element.getPropertyValue());
            structureInfo.naturalGenerationInfo.ySelector.minY = range.getMin();
            structureInfo.naturalGenerationInfo.ySelector.maxY = range.getMax();
        }
    }
}
