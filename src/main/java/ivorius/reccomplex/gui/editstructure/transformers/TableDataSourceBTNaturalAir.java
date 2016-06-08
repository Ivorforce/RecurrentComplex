/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerNaturalAir;
import ivorius.reccomplex.utils.IvTranslations;
import ivorius.reccomplex.utils.scale.Scales;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTNaturalAir extends TableDataSourceSegmented implements TableCellPropertyListener
{
    private TransformerNaturalAir transformer;

    public TableDataSourceBTNaturalAir(TransformerNaturalAir transformer)
    {
        this.transformer = transformer;

        addManagedSection(0, TableDataSourceExpression.constructDefault("Sources", transformer.sourceMatcher));
    }

    public TransformerNaturalAir getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerNaturalAir transformer)
    {
        this.transformer = transformer;
    }

    @Override
    public int numberOfSegments()
    {
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 1 ? 2 : super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 1)
        {
            switch (index)
            {
                case 0:
                {
                    TableCellFloatNullable cell = new TableCellFloatNullable("naturalExpansionDistance", TableElements.toFloat(transformer.naturalExpansionDistance), 1.0f, 0, 40, "D", "C");
                    cell.setScale(Scales.pow(5));
                    cell.addPropertyListener(this);
                    cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.naturalAir.naturalExpansionDistance.tooltip"));
                    return new TableElementCell(IvTranslations.get("reccomplex.transformer.naturalAir.naturalExpansionDistance"), cell);
                }
                case 1:
                {
                    TableCellFloatNullable cell = new TableCellFloatNullable("naturalExpansionRandomization", TableElements.toFloat(transformer.naturalExpansionRandomization), 1.0f, 0, 40, "D", "C");
                    cell.setScale(Scales.pow(5));
                    cell.addPropertyListener(this);
                    cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.naturalAir.naturalExpansionRandomization.tooltip"));
                    return new TableElementCell(IvTranslations.get("reccomplex.transformer.naturalAir.naturalExpansionRandomization"), cell);
                }
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if (cell.getID() != null)
        {
            switch (cell.getID())
            {
                case "naturalExpansionDistance":
                    transformer.naturalExpansionDistance = TableElements.toDouble((Float) cell.getPropertyValue());
                    break;
                case "naturalExpansionRandomization":
                    transformer.naturalExpansionRandomization = TableElements.toDouble((Float) cell.getPropertyValue());
                    break;
            }
        }
    }
}
