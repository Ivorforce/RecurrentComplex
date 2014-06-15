package ivorius.structuregen.gui.editstructure;

import ivorius.structuregen.gui.table.*;
import ivorius.structuregen.worldgen.blockTransformers.BlockTransformerNegativeSpace;
import net.minecraft.block.Block;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTNegativeSpace implements TableDataSource, TableElementPropertyListener
{
    private BlockTransformerNegativeSpace blockTransformer;

    public TableDataSourceBTNegativeSpace(BlockTransformerNegativeSpace blockTransformer)
    {
        this.blockTransformer = blockTransformer;
    }

    public BlockTransformerNegativeSpace getBlockTransformer()
    {
        return blockTransformer;
    }

    public void setBlockTransformer(BlockTransformerNegativeSpace blockTransformer)
    {
        this.blockTransformer = blockTransformer;
    }

    @Override
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index < 2;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index == 0)
        {
            TableElementString element = new TableElementString("sourceID", "Block", Block.blockRegistry.getNameForObject(blockTransformer.sourceBlock));
            element.setShowsValidityState(true);
            TableDataSourceBTNatural.setStateForBlockTextfield(element);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 1)
        {
            TableElementInteger element = new TableElementInteger("sourceMeta", "Metadata", blockTransformer.sourceMetadata, 0, 16);
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
    }
}
