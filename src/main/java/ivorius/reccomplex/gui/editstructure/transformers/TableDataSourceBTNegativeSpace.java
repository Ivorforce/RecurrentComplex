/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.reccomplex.gui.editstructure.TableDataSourceDimensionGen;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerNegativeSpace;
import ivorius.reccomplex.utils.IvTranslations;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTNegativeSpace extends TableDataSourceSegmented implements TableElementPropertyListener
{
    private TransformerNegativeSpace transformer;

    private TableElementTitle parsed;

    public TableDataSourceBTNegativeSpace(TransformerNegativeSpace transformer)
    {
        this.transformer = transformer;
    }

    public TransformerNegativeSpace getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerNegativeSpace transformer)
    {
        this.transformer = transformer;
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return 2;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            if (index == 0)
            {
                TableElementString element = new TableElementString("source", "Sources", transformer.sourceMatcher.getExpression());
                element.setTooltip(IvTranslations.formatLines("reccomplex.expression.block.tooltip"));
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
            {
                parsed = new TableElementTitle("parsed", "", StringUtils.abbreviate(TableDataSourceDimensionGen.parsedString(transformer.sourceMatcher), 60));
                parsed.setPositioning(TableElementTitle.Positioning.TOP);
                return parsed;
            }
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("source".equals(element.getID()))
        {
            transformer.sourceMatcher.setExpression((String) element.getPropertyValue());
            if (parsed != null)
                parsed.setDisplayString(StringUtils.abbreviate(TableDataSourceDimensionGen.parsedString(transformer.sourceMatcher), 60));
        }
    }
}
