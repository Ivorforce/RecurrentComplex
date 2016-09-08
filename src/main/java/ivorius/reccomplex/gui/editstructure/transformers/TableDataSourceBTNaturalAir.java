/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerNaturalAir;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.utils.scale.Scales;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTNaturalAir extends TableDataSourceSegmented
{
    private TransformerNaturalAir transformer;

    public TableDataSourceBTNaturalAir(TransformerNaturalAir transformer, TableNavigator navigator, TableDelegate delegate)
    {
        this.transformer = transformer;

        addManagedSection(0, new TableDataSourceTransformer(transformer, delegate, navigator));
        addManagedSection(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), transformer.sourceMatcher, null));
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
        return 3;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 2 ? 2 : super.sizeOfSegment(segment);
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 2)
        {
            switch (index)
            {
                case 0:
                {
                    TableCellFloat cell = new TableCellFloat("naturalExpansionDistance", TableElements.toFloat(transformer.naturalExpansionDistance), 0, 40);
                    cell.setScale(Scales.pow(5));
                    cell.addPropertyConsumer(val -> transformer.naturalExpansionDistance = TableElements.toDouble(val));
                    cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.naturalAir.naturalExpansionDistance.tooltip"));
                    return new TableElementCell(IvTranslations.get("reccomplex.transformer.naturalAir.naturalExpansionDistance"), cell);
                }
                case 1:
                {
                    TableCellFloat cell = new TableCellFloat("naturalExpansionRandomization", TableElements.toFloat(transformer.naturalExpansionRandomization), 0, 40);
                    cell.setScale(Scales.pow(5));
                    cell.addPropertyConsumer(val -> transformer.naturalExpansionRandomization = TableElements.toDouble(val));
                    cell.setTooltip(IvTranslations.formatLines("reccomplex.transformer.naturalAir.naturalExpansionRandomization.tooltip"));
                    return new TableElementCell(IvTranslations.get("reccomplex.transformer.naturalAir.naturalExpansionRandomization"), cell);
                }
            }
        }

        return super.elementForIndexInSegment(table, index, segment);
    }
}
