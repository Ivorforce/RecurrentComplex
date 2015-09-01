/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import ivorius.reccomplex.utils.IvTranslations;
import ivorius.reccomplex.utils.scale.Scales;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceDimensionGen extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private DimensionGenerationInfo generationInfo;

    private TableDelegate tableDelegate;

    public TableDataSourceDimensionGen(DimensionGenerationInfo generationInfo, TableDelegate tableDelegate)
    {
        this.generationInfo = generationInfo;
        this.tableDelegate = tableDelegate;
        
        addManagedSection(0, TableDataSourceExpression.constructDefault("Dimensions", generationInfo.getDimensionMatcher()));
    }

    @Override
    public int numberOfSegments()
    {
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 1 ? 1 : super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 1)
        {
            TableCellFloatNullable cell = new TableCellFloatNullable("weight", TableElements.toFloat(generationInfo.getGenerationWeight()), 1.0f, 0, 1000, "D", "C");
            cell.setScale(Scales.pow(5));
            cell.addPropertyListener(this);
            cell.setTooltip(IvTranslations.formatLines("structures.gui.random.weight.tooltip"));
            return new TableElementCell(IvTranslations.get("structures.gui.random.weight"), cell);
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("weight".equals(cell.getID()))
        {
            generationInfo.setGenerationWeight(TableElements.toDouble((Float) cell.getPropertyValue()));
        }
    }
}
