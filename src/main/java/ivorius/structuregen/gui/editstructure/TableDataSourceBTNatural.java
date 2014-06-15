package ivorius.structuregen.gui.editstructure;

import ivorius.structuregen.gui.GuiValidityStateIndicator;
import ivorius.structuregen.gui.table.*;
import ivorius.structuregen.worldgen.blockTransformers.BlockTransformerNatural;
import net.minecraft.block.Block;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceBTNatural implements TableDataSource, TableElementPropertyListener
{
    private BlockTransformerNatural blockTransformer;

    public TableDataSourceBTNatural(BlockTransformerNatural blockTransformer)
    {
        this.blockTransformer = blockTransformer;
    }

    public BlockTransformerNatural getBlockTransformer()
    {
        return blockTransformer;
    }

    public void setBlockTransformer(BlockTransformerNatural blockTransformer)
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
            setStateForBlockTextfield(element);
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
            setStateForBlockTextfield(((TableElementString) element));
        }
        else if ("sourceMeta".equals(element.getID()))
        {
            blockTransformer.sourceMetadata = (int) element.getPropertyValue();
        }
    }

    public static void setStateForBlockTextfield(TableElementString elementString)
    {
        elementString.setValidityState(stateForBlock(elementString.getPropertyValue()));
    }

    public static GuiValidityStateIndicator.State stateForBlock(String blockID)
    {
        return Block.blockRegistry.containsKey(blockID) ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID;
    }
}
