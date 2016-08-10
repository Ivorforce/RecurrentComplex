/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes.staticgen;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.gentypes.StaticGenerationInfo;
import ivorius.reccomplex.utils.IvTranslations;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceStaticPattern extends TableDataSourceSegmented implements TableCellPropertyListener
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
                    cell.addPropertyListener(this);
                    cell.setTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.repeat"));
                    return new TableElementCell("Repeat (x)", cell);
                }
                else if (index == 1)
                {
                    TableCellStringInt cell = new TableCellStringInt("repeatZ", pattern.repeatZ);
                    cell.addPropertyListener(this);
                    cell.setTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.repeat"));
                    return new TableElementCell("Repeat (z)", cell);
                }
            }
            case 1:
            {
                if (index == 0)
                {
                    TableCellInteger cell = new TableCellInteger("shiftX", pattern.randomShiftX, 0, 10);
                    cell.addPropertyListener(this);
                    cell.setTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.rshift"));
                    return new TableElementCell("Random shift (x)", cell);
                }
                else if (index == 1)
                {
                    TableCellInteger cell = new TableCellInteger("shiftZ", pattern.randomShiftZ, 0, 10);
                    cell.addPropertyListener(this);
                    cell.setTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.rshift"));
                    return new TableElementCell("Random shift (z)", cell);
                }
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if(cell.getID() != null)
        {
            switch (cell.getID())
            {
                case "repeatX":
                    pattern.repeatX = (Integer) cell.getPropertyValue();
                    break;
                case "repeatZ":
                    pattern.repeatZ = (Integer) cell.getPropertyValue();
                    break;
                case "shiftX":
                    pattern.randomShiftX = (Integer) cell.getPropertyValue();
                    break;
                case "shiftZ":
                    pattern.randomShiftZ = (Integer) cell.getPropertyValue();
                    break;
            }
        }
    }
}
