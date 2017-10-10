/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.editstructure.TableDataSourceBiomeGenList;
import ivorius.reccomplex.gui.editstructure.TableDataSourceDimensionGenList;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellEnum;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.VanillaDecorationGeneration;
import ivorius.reccomplex.world.gen.feature.decoration.RCBiomeDecorator;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceVanillaDecorationGeneration extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private VanillaDecorationGeneration generationInfo;

    public TableDataSourceVanillaDecorationGeneration(TableNavigator navigator, TableDelegate delegate, VanillaDecorationGeneration generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = delegate;
        this.generationInfo = generationInfo;

        addManagedSegment(0, new TableDataSourceGenerationType(generationInfo, navigator, delegate));

        addManagedSegment(3, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceBiomeGenList(generationInfo.biomeWeights, delegate, navigator), () -> IvTranslations.get("reccomplex.gui.biomes"))
                .buildDataSource());

        addManagedSegment(4, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceDimensionGenList(generationInfo.dimensionWeights, delegate, navigator), () -> IvTranslations.get("reccomplex.gui.dimensions"))
                .buildDataSource());

        addManagedSegment(5, new TableDataSourceBlockPos(generationInfo.spawnShift, generationInfo::setSpawnShift, null, null, null,
                IvTranslations.get("reccomplex.generationInfo.vanilla.shift.x"), IvTranslations.get("reccomplex.generationInfo.vanilla.shift.y"), IvTranslations.get("reccomplex.generationInfo.vanilla.shift.z")));
    }

    @Override
    public int numberOfSegments()
    {
        return 6;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 1:
                return 1;
            case 2:
                return 1;
        }
        return super.sizeOfSegment(segment);
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
            {
                TableCellEnum<RCBiomeDecorator.DecorationType> cell = new TableCellEnum<>("type", generationInfo.type, TableCellEnum.options(RCBiomeDecorator.DecorationType.values(), "reccomplex.generationInfo.decoration.types.", true));
                cell.addListener(v -> generationInfo.type = v);
                return new TitledCell(IvTranslations.get("reccomplex.generationInfo.vanilla.type"), cell);
            }
            case 2:
            {
                return RCGuiTables.defaultWeightElement(val -> generationInfo.generationWeight = TableCells.toDouble(val), generationInfo.generationWeight);
            }
        }

        return super.cellForIndexInSegment(table, index, segment);
    }
}
