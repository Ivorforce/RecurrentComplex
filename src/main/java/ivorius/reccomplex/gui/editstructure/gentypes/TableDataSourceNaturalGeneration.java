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
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellEnum;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.selector.NaturalStructureSelector;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.NaturalGeneration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lukas on 07.10.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceNaturalGeneration extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;
    private NaturalGeneration generationInfo;

    public TableDataSourceNaturalGeneration(TableNavigator navigator, TableDelegate delegate, NaturalGeneration generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = delegate;
        this.generationInfo = generationInfo;

        addSegment(0, new TableDataSourceGenerationType(generationInfo, navigator, delegate));

        addSegment(1, () -> {
            TableCellEnum<String> cell = new TableCellEnum<>("category", generationInfo.generationCategory, allGenerationCategories());
            cell.addListener(val -> generationInfo.generationCategory = val);
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.natural.category"), cell);
        });

        addSegment(2, () -> {
            return RCGuiTables.defaultWeightElement(val -> generationInfo.setGenerationWeight(TableCells.toDouble(val)), generationInfo.getGenerationWeight());
        });

        addSegment(3, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceSelectivePlacer(generationInfo.placer, navigator, delegate)).withTitle(IvTranslations.get("reccomplex.placer")).buildDataSource());

        addSegment(4, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceBiomeGenList(generationInfo.biomeWeights, delegate, navigator)).withTitle(IvTranslations.get("reccomplex.gui.biomes")).buildDataSource());

        addSegment(5, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceDimensionGenList(generationInfo.dimensionWeights, delegate, navigator)).withTitle(IvTranslations.get("reccomplex.gui.dimensions")).buildDataSource());

        addSegment(6, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceNaturalGenLimitation(generationInfo.spawnLimitation, delegate))
                .enabled(generationInfo::hasLimitations)
                .addAction(() -> generationInfo.spawnLimitation = generationInfo.hasLimitations() ? null : new NaturalGeneration.SpawnLimitation(), () -> generationInfo.hasLimitations() ? IvTranslations.get("reccomplex.gui.remove") : IvTranslations.get("reccomplex.gui.add"), null
                ).withTitle(IvTranslations.get("reccomplex.generationInfo.natural.limitations")).buildDataSource());
    }

    public static List<TableCellEnum.Option<String>> allGenerationCategories()
    {
        Set<String> categories = NaturalStructureSelector.CATEGORY_REGISTRY.activeIDs();
        return categories.stream()
                .map(category -> Pair.of(category, NaturalStructureSelector.CATEGORY_REGISTRY.getActive(category)))
                .filter(p -> p.getRight().selectableInGUI())
                .map(p -> new TableCellEnum.Option<>(p.getLeft(), p.getRight().title(), p.getRight().tooltip()))
                .sorted(Comparator.comparing(o -> o.title))
                .collect(Collectors.toList());
    }
}
