/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes.staticgen;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.StaticGenerationInfo;
import ivorius.ivtoolkit.tools.IvTranslations;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceStaticPattern extends TableDataSourceSegmented
{
    private StaticGenerationInfo.Pattern pattern;

    private TableDelegate tableDelegate;

    public TableDataSourceStaticPattern(StaticGenerationInfo.Pattern pattern, TableDelegate tableDelegate)
    {
        this.pattern = pattern;
        this.tableDelegate = tableDelegate;
    }

    @Override
    public int numberOfSegments()
    {
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
            case 1:
                return 2;
            default:
                return super.sizeOfSegment(segment);
        }
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 0:
            {
                if (index == 0)
                {
                    TableCellStringInt cell = new TableCellStringInt("repeatX", pattern.repeatX);
                    cell.addPropertyConsumer(val -> pattern.repeatX = val);
                    return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.static.pattern.repeat.x"), cell)
                            .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.repeat.tooltip"));
                }
                else if (index == 1)
                {
                    TableCellStringInt cell = new TableCellStringInt("repeatZ", pattern.repeatZ);
                    cell.addPropertyConsumer(val -> pattern.repeatZ = val);
                    return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.static.pattern.repeat.z"), cell)
                            .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.repeat.tooltip"));
                }
            }
            case 1:
            {
                if (index == 0)
                {
                    TableCellInteger cell = new TableCellInteger("shiftX", pattern.randomShiftX, 0, 10);
                    cell.addPropertyConsumer(val -> pattern.randomShiftX = val);
                    return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.static.pattern.rshift.x"), cell)
                            .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.rshift.tooltip"));
                }
                else if (index == 1)
                {
                    TableCellInteger cell = new TableCellInteger("shiftZ", pattern.randomShiftZ, 0, 10);
                    cell.addPropertyConsumer(val -> pattern.randomShiftZ = val);
                    return new TableElementCell(IvTranslations.get("reccomplex.generationInfo.static.pattern.rshift.z"), cell)
                            .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.rshift.tooltip"));
                }
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
