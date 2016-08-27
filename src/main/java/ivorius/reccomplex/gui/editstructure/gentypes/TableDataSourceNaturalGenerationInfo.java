/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.editstructure.TableDataSourceBiomeGenList;
import ivorius.reccomplex.gui.editstructure.TableDataSourceDimensionGenList;
import ivorius.reccomplex.gui.editstructure.TableDataSourceNaturalGenLimitation;
import ivorius.reccomplex.gui.editstructure.TableDataSourceYSelector;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.worldgen.StructureSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceNaturalGenerationInfo extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;
    private NaturalGenerationInfo generationInfo;

    public TableDataSourceNaturalGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, NaturalGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSection(0, new TableDataSourceGenerationInfo(generationInfo, navigator, tableDelegate));
        addManagedSection(2, new TableDataSourceYSelector(generationInfo.ySelector));

        addManagedSection(4, TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> IvTranslations.get("reccomplex.gui.edit"), null,
                        () -> new GuiTable(tableDelegate, new TableDataSourceBiomeGenList(generationInfo.biomeWeights, tableDelegate, navigator))
                ).buildDataSource(IvTranslations.get("reccomplex.gui.biomes")));

        addManagedSection(5, TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> IvTranslations.get("reccomplex.gui.edit"), null,
                        () -> new GuiTable(tableDelegate, new TableDataSourceDimensionGenList(generationInfo.dimensionWeights, tableDelegate, navigator))
                ).buildDataSource(IvTranslations.get("reccomplex.gui.dimensions")));

        addManagedSection(6, TableCellMultiBuilder.create(navigator, tableDelegate)
                .addNavigation(() -> IvTranslations.get("reccomplex.gui.edit"), null,
                        () -> new GuiTable(tableDelegate, new TableDataSourceNaturalGenLimitation(generationInfo.spawnLimitation, tableDelegate))
                ).enabled(generationInfo::hasLimitations)
                .addAction(() -> generationInfo.hasLimitations() ? "Remove" : "Add", null,
                        () -> generationInfo.spawnLimitation = generationInfo.hasLimitations() ? null : new NaturalGenerationInfo.SpawnLimitation()
                ).buildDataSource(IvTranslations.get("reccomplex.generationInfo.natural.limitations")));
    }

    public static List<TableCellEnum.Option<String>> allGenerationCategories()
    {
        Set<String> categories = StructureSelector.allCategoryIDs();
        List<TableCellEnum.Option<String>> generationCategories = new ArrayList<>();

        for (String category : categories)
        {
            StructureSelector.Category categoryObj = StructureSelector.categoryForID(category);

            if (categoryObj.selectableInGUI())
                generationCategories.add(new TableCellEnum.Option<>(category, categoryObj.title(), categoryObj.tooltip()));
        }

        return generationCategories;
    }

    @Override
    public int numberOfSegments()
    {
        return 7;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
            case 3:
                return 1;
        }

        return super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
            {
                TableCellEnum cell = new TableCellEnum<>("category", generationInfo.generationCategory, allGenerationCategories());
                cell.addPropertyListener(this);
                return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.natural.category"), cell);
            }
            case 3:
                return RCGuiTables.defaultWeightElement(this, generationInfo.getGenerationWeight());
        }

        return super.elementForIndexInSegment(table, index, segment);
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
