/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.transformers;

import ivorius.ivtoolkit.gui.FloatRange;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.transformers.TransformerRuins;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTRuins extends TableDataSourceSegmented implements TableElementPropertyListener
{
    private TransformerRuins transformer;

    public TableDataSourceBTRuins(TransformerRuins transformer)
    {
        this.transformer = transformer;
    }

    public TransformerRuins getTransformer()
    {
        return transformer;
    }

    public void setTransformer(TransformerRuins transformer)
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
        return 4;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (index)
        {
            case 0:
            {
                TableElementFloatRange element = new TableElementFloatRange("decay", "Decay", new FloatRange(transformer.minDecay, transformer.maxDecay), 0.0f, 1.0f, 2);
                element.addPropertyListener(this);
                return element;
            }
            case 1:
            {
                TableElementFloat element = new TableElementFloat("decayChaos", "Chaos", transformer.decayChaos, 0.0f, 1.0f);
                element.addPropertyListener(this);
                return element;
            }
            case 2:
            {
                TableElementFloat element = new TableElementFloat("erosion", "Erosion", transformer.blockErosion, 0.0f, 1.0f);
                element.addPropertyListener(this);
                return element;
            }
            case 3:
            {
                TableElementFloat element = new TableElementFloat("vines", "Vine Growth", transformer.vineGrowth, 0.0f, 1.0f);
                element.addPropertyListener(this);
                return element;
            }
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        switch (element.getID())
        {
            case "decay":
                FloatRange range = (FloatRange) element.getPropertyValue();
                transformer.minDecay = range.getMin();
                transformer.maxDecay = range.getMax();
                break;
            case "decayChaos":
                transformer.decayChaos = (float) element.getPropertyValue();
                break;
            case "erosion":
                transformer.blockErosion = (float) element.getPropertyValue();
                break;
            case "vines":
                transformer.vineGrowth = (float) element.getPropertyValue();
                break;
        }
    }
}
