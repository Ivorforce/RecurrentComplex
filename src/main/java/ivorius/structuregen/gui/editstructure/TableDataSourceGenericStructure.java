/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.gui.editstructure;

import ivorius.structuregen.gui.GuiValidityStateIndicator;
import ivorius.structuregen.gui.IntegerRange;
import ivorius.structuregen.gui.table.*;
import ivorius.structuregen.worldgen.StructureHandler;
import ivorius.structuregen.worldgen.genericStructures.GenerationYSelector;
import ivorius.structuregen.worldgen.genericStructures.GenericStructureInfo;
import joptsimple.internal.Strings;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceGenericStructure implements TableDataSource, TableElementButton.Listener, TableElementPropertyListener
{
    private GenericStructureInfo structureInfo;
    private String structureKey;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    public TableDataSourceGenericStructure(GenericStructureInfo structureInfo, String structureKey, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.structureInfo = structureInfo;
        this.structureKey = structureKey;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;
    }

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
        String[] categories = {"decoration", "adventure", "rare"};
        List<TableElementList.Option> generationBases = new ArrayList<>();

        for (String category : categories)
        {
            generationBases.add(new TableElementList.Option(category, I18n.format("structures.category." + category)));
        }

        return generationBases;
    }

    public GenericStructureInfo getStructureInfo()
    {
        return structureInfo;
    }

    public void setStructureInfo(GenericStructureInfo structureInfo)
    {
        this.structureInfo = structureInfo;
    }

    public String getStructureKey()
    {
        return structureKey;
    }

    public void setStructureKey(String structureKey)
    {
        this.structureKey = structureKey;
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
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index < 9;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index == 0)
        {
            TableElementString element = new TableElementString("name", "Name", structureKey);
            element.addPropertyListener(this);
            element.setShowsValidityState(true);
            element.setValidityState(currentNameState());
            return element;
        }
        else if (index == 1)
        {
            TableElementList element = new TableElementList("category", "Category", structureInfo.generationCategory(), allGenerationCategories());
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 2)
        {
            TableElementList element = new TableElementList("ySelType", "Generation Base", structureInfo.ySelector.selectionMode.serializedName(), allGenerationOptions());
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 3)
        {
            TableElementIntegerRange element = new TableElementIntegerRange("ySelShift", "Y Shift", new IntegerRange(structureInfo.ySelector.minY, structureInfo.ySelector.maxY), -100, 100);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 4)
        {
            TableElementBoolean element = new TableElementBoolean("rotatable", "Rotatable", structureInfo.rotatable);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 5)
        {
            TableElementBoolean element = new TableElementBoolean("mirrorable", "Mirrorable", structureInfo.mirrorable);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 6)
        {
            TableElementString element = new TableElementString("dependencies", "Dependencies (A,B,...)", Strings.join(structureInfo.dependencies, ","));
            element.setValidityState(currentDependencyState());
            element.setShowsValidityState(true);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 7)
        {
            TableElementButton elementEditBiomes = new TableElementButton("editBiomes", "Biomes", new TableElementButton.Action("edit", "Edit"));
            elementEditBiomes.addListener(this);
            return elementEditBiomes;
        }
        else if (index == 8)
        {
            TableElementButton elementEditTransformers = new TableElementButton("editTransformers", "Transformers", new TableElementButton.Action("edit", "Edit"));
            elementEditTransformers.addListener(this);
            return elementEditTransformers;
        }

        return null;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if ("editBiomes".equals(tableElementButton.getID()) && "edit".equals(actionID))
        {
            GuiTable editBiomesProperties = new GuiTable(tableDelegate, new TableDataSourceBiomeGenList(structureInfo.generationWeights, tableDelegate, navigator));
            navigator.pushTable(editBiomesProperties);
        }
        else if ("editTransformers".equals(tableElementButton.getID()) && "edit".equals(actionID))
        {
            GuiTable editTransformersProperties = new GuiTable(tableDelegate, new TableDataSourceBlockTransformerList(structureInfo.blockTransformers, tableDelegate, navigator));
            navigator.pushTable(editTransformersProperties);
        }
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("name".equals(element.getID()))
        {
            structureKey = (String) element.getPropertyValue();
            ((TableElementString) element).setValidityState(currentNameState());
        }
        else if ("category".equals(element.getID()))
        {
            structureInfo.generationCategory = (String) element.getPropertyValue();
        }
        else if ("ySelType".equals(element.getID()))
        {
            GenerationYSelector.SelectionMode selectionMode = GenerationYSelector.SelectionMode.selectionMode((String) element.getPropertyValue());
            structureInfo.ySelector.selectionMode = selectionMode != null ? selectionMode : GenerationYSelector.SelectionMode.SURFACE;
        }
        else if ("ySelShift".equals(element.getID()))
        {
            IntegerRange range = ((IntegerRange) element.getPropertyValue());
            structureInfo.ySelector.minY = range.getMin();
            structureInfo.ySelector.maxY = range.getMax();
        }
        else if ("rotatable".equals(element.getID()))
        {
            structureInfo.rotatable = (boolean) element.getPropertyValue();
        }
        else if ("mirrorable".equals(element.getID()))
        {
            structureInfo.mirrorable = (boolean) element.getPropertyValue();
        }
        else if ("dependencies".equals(element.getID()))
        {
            structureInfo.dependencies.clear();
            String[] dependencies = ((String) element.getPropertyValue()).split(",");
            if (dependencies.length != 1 || dependencies[0].trim().length() > 0)
            {
                Collections.addAll(structureInfo.dependencies, dependencies);
            }

            ((TableElementString) element).setValidityState(currentDependencyState());
        }
    }

    private GuiValidityStateIndicator.State currentNameState()
    {
        if (StructureHandler.getAllStructureNames().contains(structureKey))
        {
            return GuiValidityStateIndicator.State.SEMI_VALID;
        }

        return structureKey.trim().length() > 0 ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID;
    }

    private GuiValidityStateIndicator.State currentDependencyState()
    {
        return structureInfo.areDependenciesResolved() ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.SEMI_VALID;
    }
}
