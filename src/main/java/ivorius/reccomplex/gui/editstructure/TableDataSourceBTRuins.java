/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.gui.FloatRange;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.blocktransformers.BlockTransformerRuins;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTRuins implements TableDataSource, TableElementPropertyListener
{
    private BlockTransformerRuins blockTransformer;

    public TableDataSourceBTRuins(BlockTransformerRuins blockTransformer)
    {
        this.blockTransformer = blockTransformer;
    }

    public BlockTransformerRuins getBlockTransformer()
    {
        return blockTransformer;
    }

    public void setBlockTransformer(BlockTransformerRuins blockTransformer)
    {
        this.blockTransformer = blockTransformer;
    }

    @Override
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index < 4;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        switch (index)
        {
            case 0:
            {
                TableElementFloatRange element = new TableElementFloatRange("decay", "Decay", new FloatRange(blockTransformer.minDecay, blockTransformer.maxDecay), 0.0f, 1.0f, 2);
                element.addPropertyListener(this);
                return element;
            }
            case 1:
            {
                TableElementFloat element = new TableElementFloat("decayChaos", "Chaos", blockTransformer.decayChaos, 0.0f, 1.0f);
                element.addPropertyListener(this);
                return element;
            }
            case 2:
            {
                TableElementFloat element = new TableElementFloat("erosion", "Erosion", blockTransformer.blockErosion, 0.0f, 1.0f);
                element.addPropertyListener(this);
                return element;
            }
            case 3:
            {
                TableElementFloat element = new TableElementFloat("vines", "Vine Growth", blockTransformer.vineGrowth, 0.0f, 1.0f);
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
                blockTransformer.minDecay = range.getMin();
                blockTransformer.maxDecay = range.getMax();
                break;
            case "decayChaos":
                blockTransformer.decayChaos = (float) element.getPropertyValue();
                break;
            case "erosion":
                blockTransformer.blockErosion = (float) element.getPropertyValue();
                break;
            case "vines":
                blockTransformer.vineGrowth = (float) element.getPropertyValue();
                break;
        }
    }
}
