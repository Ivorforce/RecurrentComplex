/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.reccomplex.gui.editstructure.TableDataSourceBiomeGenList;
import ivorius.reccomplex.gui.editstructure.TableDataSourceDimensionGenList;
import ivorius.reccomplex.gui.editstructure.TableDataSourceYSelector;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.GenerationYSelector;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.utils.IvTranslations;
import ivorius.reccomplex.worldgen.StructureSelector;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceNaturalGenerationInfo extends TableDataSourceSegmented implements TableElementActionListener, TableElementPropertyListener
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;
    private NaturalGenerationInfo generationInfo;

    public TableDataSourceNaturalGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, NaturalGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSection(0, new TableDataSourceGenerationInfo(generationInfo));
        addManagedSection(2, new TableDataSourceYSelector(generationInfo.ySelector));
    }

    public static List<TableElementList.Option> allGenerationCategories()
    {
        Set<String> categories = StructureSelector.allCategoryIDs();
        List<TableElementList.Option> generationBases = new ArrayList<>();

        for (String category : categories)
        {
            StructureSelector.Category categoryObj = StructureSelector.categoryForID(category);

            if (categoryObj.selectableInGUI())
            {
                String transKeyBase = "structures.category." + category;

                generationBases.add(new TableElementList.Option(category,
                        I18n.format(transKeyBase), IvTranslations.formatLines(transKeyBase + ".tooltip")));
            }
        }

        return generationBases;
    }

    @Override
    public int numberOfSegments()
    {
        return 4;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
                return 1;
            case 3:
                return 3;
        }

        return super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
                if (index == 0)
                {
                    TableElementList element = new TableElementList("category", "Category", generationInfo.generationCategory, allGenerationCategories());
                    element.addPropertyListener(this);
                    return element;
                }
                break;
            case 3:
                if (index == 0)
                {
                    TableElementFloatNullable element = new TableElementFloatNullable("weight", "Weight", TableElements.toFloat(generationInfo.getGenerationWeight()), 1.0f, 0, 10, "D", "C");
                    element.addPropertyListener(this);
                    return element;
                }
                else if (index == 1)
                {
                    TableElementButton elementEditBiomes = new TableElementButton("editBiomes", "Biomes", new TableElementButton.Action("edit", "Edit"));
                    elementEditBiomes.addListener(this);
                    return elementEditBiomes;
                }
                else if (index == 2)
                {
                    TableElementButton elementEditBiomes = new TableElementButton("editDimensions", "Dimensions", new TableElementButton.Action("edit", "Edit"));
                    elementEditBiomes.addListener(this);
                    return elementEditBiomes;
                }
                break;
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void actionPerformed(TableElement element, String actionID)
    {
        if ("editBiomes".equals(element.getID()) && "edit".equals(actionID))
        {
            GuiTable editBiomesProperties = new GuiTable(tableDelegate, new TableDataSourceBiomeGenList(generationInfo.biomeWeights, tableDelegate, navigator));
            navigator.pushTable(editBiomesProperties);
        }
        else if ("editDimensions".equals(element.getID()) && "edit".equals(actionID))
        {
            GuiTable editBiomesProperties = new GuiTable(tableDelegate, new TableDataSourceDimensionGenList(generationInfo.dimensionWeights, tableDelegate, navigator));
            navigator.pushTable(editBiomesProperties);
        }
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        switch (element.getID())
        {
            case "category":
                generationInfo.generationCategory = (String) element.getPropertyValue();
                break;
            case "weight":
                generationInfo.setGenerationWeight(TableElements.toDouble((Float) element.getPropertyValue()));
                break;
        }
    }
}
