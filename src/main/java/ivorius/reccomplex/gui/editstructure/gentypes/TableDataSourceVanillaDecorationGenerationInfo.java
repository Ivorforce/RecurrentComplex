/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.editstructure.TableDataSourceBiomeGenList;
import ivorius.reccomplex.gui.editstructure.TableDataSourceDimensionGenList;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.VanillaDecorationGenerationInfo;
import ivorius.reccomplex.world.gen.feature.decoration.RCBiomeDecorator;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceVanillaDecorationGenerationInfo extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private VanillaDecorationGenerationInfo generationInfo;

    public TableDataSourceVanillaDecorationGenerationInfo(TableNavigator navigator, TableDelegate delegate, VanillaDecorationGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = delegate;
        this.generationInfo = generationInfo;

        addManagedSegment(0, new TableDataSourceGenerationInfo(generationInfo, navigator, delegate));

        addManagedSegment(3, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceBiomeGenList(generationInfo.biomeWeights, delegate, navigator)
                ).buildDataSource(IvTranslations.get("reccomplex.gui.biomes")));

        addManagedSegment(4, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceDimensionGenList(generationInfo.dimensionWeights, delegate, navigator)
                ).buildDataSource(IvTranslations.get("reccomplex.gui.dimensions")));

        addManagedSegment(5, new TableDataSourceBlockPos(generationInfo.spawnShift, generationInfo::setSpawnShift, new IntegerRange(-50, 50), new IntegerRange(-50, 50), new IntegerRange(-50, 50),
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
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 1:
            {
                TableCellEnum<RCBiomeDecorator.DecorationType> cell = new TableCellEnum<>("type", generationInfo.type, TableCellEnum.options(RCBiomeDecorator.DecorationType.values(), "reccomplex.generationInfo.decoration.types.", true));
                cell.addPropertyConsumer(v -> generationInfo.type = v);
                return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.vanilla.type"), cell);
            }
            case 2:
            {
                return RCGuiTables.defaultWeightElement(val -> generationInfo.generationWeight = TableElements.toDouble(val), generationInfo.generationWeight);
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
