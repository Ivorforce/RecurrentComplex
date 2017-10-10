/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes.staticgen;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellInteger;
import ivorius.reccomplex.gui.table.cell.TableCellStringInt;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.StaticGeneration;
import ivorius.ivtoolkit.tools.IvTranslations;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceStaticPattern extends TableDataSourceSegmented
{
    private StaticGeneration.Pattern pattern;

    private TableDelegate tableDelegate;

    public TableDataSourceStaticPattern(StaticGeneration.Pattern pattern, TableDelegate tableDelegate)
    {
        this.pattern = pattern;
        this.tableDelegate = tableDelegate;
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Static Pattern";
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
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 0:
            {
                if (index == 0)
                {
                    TableCellStringInt cell = new TableCellStringInt("repeatX", pattern.repeatX);
                    cell.addListener(val -> pattern.repeatX = val);
                    return new TitledCell(IvTranslations.get("reccomplex.generationInfo.static.pattern.repeat.x"), cell)
                            .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.repeat.tooltip"));
                }
                else if (index == 1)
                {
                    TableCellStringInt cell = new TableCellStringInt("repeatZ", pattern.repeatZ);
                    cell.addListener(val -> pattern.repeatZ = val);
                    return new TitledCell(IvTranslations.get("reccomplex.generationInfo.static.pattern.repeat.z"), cell)
                            .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.repeat.tooltip"));
                }
            }
            case 1:
            {
                if (index == 0)
                {
                    TableCellInteger cell = new TableCellInteger("shiftX", pattern.randomShiftX, 0, 10);
                    cell.addListener(val -> pattern.randomShiftX = val);
                    return new TitledCell(IvTranslations.get("reccomplex.generationInfo.static.pattern.rshift.x"), cell)
                            .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.rshift.tooltip"));
                }
                else if (index == 1)
                {
                    TableCellInteger cell = new TableCellInteger("shiftZ", pattern.randomShiftZ, 0, 10);
                    cell.addListener(val -> pattern.randomShiftZ = val);
                    return new TitledCell(IvTranslations.get("reccomplex.generationInfo.static.pattern.rshift.z"), cell)
                            .withTitleTooltip(IvTranslations.getLines("reccomplex.generationInfo.static.pattern.rshift.tooltip"));
                }
            }
        }

        return super.cellForIndexInSegment(table, index, segment);
    }
}
