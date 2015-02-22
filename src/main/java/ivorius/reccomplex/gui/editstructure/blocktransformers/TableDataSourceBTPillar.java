/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure.blocktransformers;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.blocktransformers.BlockTransformerPillar;
import net.minecraft.block.Block;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTPillar implements TableDataSource, TableElementPropertyListener
{
    private BlockTransformerPillar blockTransformer;

    public TableDataSourceBTPillar(BlockTransformerPillar blockTransformer)
    {
        this.blockTransformer = blockTransformer;
    }

    public BlockTransformerPillar getBlockTransformer()
    {
        return blockTransformer;
    }

    public void setBlockTransformer(BlockTransformerPillar blockTransformer)
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
        if (index == 0)
        {
            TableElementString element = new TableElementString("sourceID", "Source Block", Block.blockRegistry.getNameForObject(blockTransformer.sourceBlock));
            element.setShowsValidityState(true);
            TableDataSourceBTNatural.setStateForBlockTextfield(element);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 1)
        {
            TableElementInteger element = new TableElementInteger("sourceMeta", "Source Metadata", blockTransformer.sourceMetadata, 0, 16);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 2)
        {
            TableElementString element = new TableElementString("destID", "Dest Block", Block.blockRegistry.getNameForObject(blockTransformer.destBlock));
            element.setShowsValidityState(true);
            TableDataSourceBTNatural.setStateForBlockTextfield(element);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 3)
        {
            TableElementInteger element = new TableElementInteger("destMeta", "Dest Metadata", blockTransformer.destMetadata, 0, 16);
            element.addPropertyListener(this);
            return element;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("sourceID".equals(element.getID()))
        {
            blockTransformer.sourceBlock = (Block) Block.blockRegistry.getObject(element.getPropertyValue());
            TableDataSourceBTNatural.setStateForBlockTextfield(((TableElementString) element));
        }
        else if ("sourceMeta".equals(element.getID()))
        {
            blockTransformer.sourceMetadata = (int) element.getPropertyValue();
        }
        else if ("destID".equals(element.getID()))
        {
            blockTransformer.destBlock = (Block) Block.blockRegistry.getObject(element.getPropertyValue());
            TableDataSourceBTNatural.setStateForBlockTextfield(((TableElementString) element));
        }
        else if ("destMeta".equals(element.getID()))
        {
            blockTransformer.destMetadata = (int) element.getPropertyValue();
        }
    }
}
