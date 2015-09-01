/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.reccomplex.gui.editstructure.TableDataSourceBiomeGenList;
import ivorius.reccomplex.gui.editstructure.TableDataSourceDimensionGenList;
import ivorius.reccomplex.gui.editstructure.TableDataSourceNaturalGenLimitation;
import ivorius.reccomplex.gui.editstructure.TableDataSourceYSelector;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.utils.IvTranslations;
import ivorius.reccomplex.utils.scale.Scales;
import ivorius.reccomplex.worldgen.StructureSelector;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceNaturalGenerationInfo extends TableDataSourceSegmented implements TableCellActionListener, TableCellPropertyListener
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

    public static List<TableCellEnum.Option<String>> allGenerationCategories()
    {
        Set<String> categories = StructureSelector.allCategoryIDs();
        List<TableCellEnum.Option<String>> generationCategories = new ArrayList<>();

        for (String category : categories)
        {
            StructureSelector.Category categoryObj = StructureSelector.categoryForID(category);

            if (categoryObj.selectableInGUI())
            {
                String transKeyBase = "structures.category." + category;

                generationCategories.add(new TableCellEnum.Option<>(category,
                        I18n.format(transKeyBase), IvTranslations.formatLines(transKeyBase + ".tooltip")));
            }
        }

        return generationCategories;
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
                return 4;
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
                    TableCellEnum cell = new TableCellEnum<>("category", generationInfo.generationCategory, allGenerationCategories());
                    cell.addPropertyListener(this);
                    return new TableElementCell("Category", cell);
                }
                break;
            case 3:
                if (index == 0)
                {
                    TableCellFloatNullable cell = new TableCellFloatNullable("weight", TableElements.toFloat(generationInfo.getGenerationWeight()), 1.0f, 0, 1000, "D", "C");
                    cell.setScale(Scales.pow(5));
                    cell.addPropertyListener(this);
                    cell.setTooltip(IvTranslations.formatLines("structures.gui.random.weight.tooltip"));
                    return new TableElementCell(IvTranslations.get("structures.gui.random.weight"), cell);
                }
                else if (index == 1)
                {
                    TableCellButton cell = new TableCellButton("editBiomes", new TableCellButton.Action("edit", "Edit"));
                    cell.addListener(this);
                    return new TableElementCell("Biomes", cell);
                }
                else if (index == 2)
                {
                    TableCellButton cell = new TableCellButton("editDimensions", new TableCellButton.Action("edit", "Edit"));
                    cell.addListener(this);
                    return new TableElementCell("Dimensions", cell);
                }
                else if (index == 3)
                {
                    TableCellButton cell = new TableCellButton("editLimitations", new TableCellButton.Action("edit", "Edit", generationInfo.hasLimitations()), generationInfo.hasLimitations() ? new TableCellButton.Action("remove", "Remove") : new TableCellButton.Action("add", "Add"));
                    cell.addListener(this);
                    return new TableElementCell("Limitations", cell);
                }
                break;
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void actionPerformed(TableCell cell, String actionID)
    {
        if ("editBiomes".equals(cell.getID()) && "edit".equals(actionID))
        {
            GuiTable table = new GuiTable(tableDelegate, new TableDataSourceBiomeGenList(generationInfo.biomeWeights, tableDelegate, navigator));
            navigator.pushTable(table);
        }
        else if ("editDimensions".equals(cell.getID()) && "edit".equals(actionID))
        {
            GuiTable table = new GuiTable(tableDelegate, new TableDataSourceDimensionGenList(generationInfo.dimensionWeights, tableDelegate, navigator));
            navigator.pushTable(table);
        }
        else if ("editLimitations".equals(cell.getID()))
        {
            switch (actionID)
            {
                case "edit":
                    GuiTable table = new GuiTable(tableDelegate, new TableDataSourceNaturalGenLimitation(generationInfo.spawnLimitation, tableDelegate));
                    navigator.pushTable(table);
                    break;
                case "remove":
                    generationInfo.spawnLimitation = null;
                    tableDelegate.reloadData();
                    break;
                case "add":
                    generationInfo.spawnLimitation = new NaturalGenerationInfo.SpawnLimitation();
                    tableDelegate.reloadData();
                    break;
            }
        }
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if (cell.getID() != null)
        {
            switch (cell.getID())
            {
                case "category":
                    generationInfo.generationCategory = (String) cell.getPropertyValue();
                    break;
                case "weight":
                    generationInfo.setGenerationWeight(TableElements.toDouble((Float) cell.getPropertyValue()));
                    break;
            }
        }
    }
}
