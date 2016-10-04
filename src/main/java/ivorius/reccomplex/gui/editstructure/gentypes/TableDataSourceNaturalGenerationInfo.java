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
import ivorius.reccomplex.gui.editstructure.placer.TableDataSourcePlacer;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellEnum;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.cell.TableElementCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.world.gen.feature.selector.NaturalStructureSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceNaturalGenerationInfo extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;
    private NaturalGenerationInfo generationInfo;

    public TableDataSourceNaturalGenerationInfo(TableNavigator navigator, TableDelegate delegate, NaturalGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = delegate;
        this.generationInfo = generationInfo;

        addManagedSegment(0, new TableDataSourceGenerationInfo(generationInfo, navigator, delegate));

        addManagedSegment(3, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourcePlacer(generationInfo.placer, delegate, navigator))
                .buildDataSource(IvTranslations.get("reccomplex.placer"), IvTranslations.getLines("reccomplex.placer.tooltip")));

        addManagedSegment(4, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceBiomeGenList(generationInfo.biomeWeights, delegate, navigator)
                ).buildDataSource(IvTranslations.get("reccomplex.gui.biomes")));

        addManagedSegment(5, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceDimensionGenList(generationInfo.dimensionWeights, delegate, navigator)
                ).buildDataSource(IvTranslations.get("reccomplex.gui.dimensions")));

        addManagedSegment(6, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceNaturalGenLimitation(generationInfo.spawnLimitation, delegate)
                ).enabled(generationInfo::hasLimitations)
                .addAction(() -> generationInfo.hasLimitations() ? IvTranslations.get("reccomplex.gui.remove") : IvTranslations.get("reccomplex.gui.add"), null,
                        () -> generationInfo.spawnLimitation = generationInfo.hasLimitations() ? null : new NaturalGenerationInfo.SpawnLimitation()
                ).buildDataSource(IvTranslations.get("reccomplex.generationInfo.natural.limitations")));
    }

    public static List<TableCellEnum.Option<String>> allGenerationCategories()
    {
        Set<String> categories = NaturalStructureSelector.CATEGORY_REGISTRY.activeIDs();
        List<TableCellEnum.Option<String>> generationCategories = new ArrayList<>();

        for (String category : categories)
        {
            NaturalStructureSelector.Category categoryObj = NaturalStructureSelector.CATEGORY_REGISTRY.getActive(category);

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
            case 2:
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
                TableCellEnum<String> cell = new TableCellEnum<>("category", generationInfo.generationCategory, allGenerationCategories());
                cell.addPropertyConsumer(val -> generationInfo.generationCategory = val);
                return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.natural.category"), cell);
            }
            case 2:
                return RCGuiTables.defaultWeightElement(val -> generationInfo.setGenerationWeight(TableElements.toDouble(val)), generationInfo.getGenerationWeight());
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
