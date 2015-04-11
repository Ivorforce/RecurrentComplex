/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.BiomeGenerationInfo;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBiomeGen extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private BiomeGenerationInfo generationInfo;

    private TableDelegate tableDelegate;

    public TableDataSourceBiomeGen(BiomeGenerationInfo generationInfo, TableDelegate tableDelegate)
    {
        this.generationInfo = generationInfo;
        this.tableDelegate = tableDelegate;

        addManagedSection(0, TableDataSourceExpression.constructDefault("Biomes", generationInfo.getBiomeMatcher()));
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
            TableCellFloatNullable element = new TableCellFloatNullable("weight", TableElements.toFloat(generationInfo.getGenerationWeight()), 1.0f, 0, 10, "D", "C");
            element.addPropertyListener(this);
            return new TableElementCell("Weight", element);
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
