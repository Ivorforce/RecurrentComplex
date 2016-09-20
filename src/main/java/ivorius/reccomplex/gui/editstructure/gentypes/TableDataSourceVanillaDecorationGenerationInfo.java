/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.VanillaDecorationGenerationInfo;
import ivorius.reccomplex.worldgen.decoration.RCBiomeDecorator;

import java.util.Arrays;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceVanillaDecorationGenerationInfo extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private VanillaDecorationGenerationInfo generationInfo;

    public TableDataSourceVanillaDecorationGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, VanillaDecorationGenerationInfo generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.generationInfo = generationInfo;

        addManagedSection(0, new TableDataSourceGenerationInfo(generationInfo, navigator, tableDelegate));
        addManagedSection(3, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.environment"), generationInfo.environmentMatcher, null));
        addManagedSection(4, new TableDataSourceBlockPos(generationInfo.spawnShift, generationInfo::setSpawnShift, new IntegerRange(-50, 50), new IntegerRange(-50, 50), new IntegerRange(-50, 50),
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
